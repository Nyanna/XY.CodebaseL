package net.xy.codebase.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

/**
 * This class re-directs all requests to PrintStream (used by STDOUT and STDERR)
 * and sends them to Commons Logging.
 *
 * The calling method to PrintStream is determined by analyzing the stack trace.
 *
 * Use the convenience methods registerOnStdout and registerOnStderr to
 * automatically create an instance of this class and register it on the stream
 * to redirect to Commons Logging.
 *
 * Example of typical use:
 *
 *
 * public static void main(String[] args){
 * CommonsLoggingPrintStream.registerOnStdout(null, "STDOUT");
 * CommonsLoggingPrintStream.registerOnStderr(null, "STDERR"); //... }
 *
 *
 * Note for the oddball cases: If you make multiple calls to methods which don't
 * trigger a flush, as explained in PrintWriter (for example, append(char)) the
 * calling method will be determined only by the final call that triggers a
 * flush or calls flush() directly. also note that in this case you must
 * synchronize access to these methods as they will not be thread safe. It's
 * generally advised to only call methods that generate an automatic flush as
 * described in the PrintWriter javadocs
 */
public class CommonsLoggingPrintStream extends PrintStream {
	LoggingOutputStream outputStream;
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * You can use a new instance to log all PrintStream methods to Commons
	 * Logging. Messages will be written to CommonsLogging when flush() is
	 * called using the same rules as PrintStream uses with autoFlush=true
	 *
	 * @param prependName
	 *            A name prepended to the class (null for none) name as in:
	 *            registerOnStdout("STDOUT", null) results in a log message such
	 *            as: INFO STDOUT.org.mydomain.MyClass - Log message
	 * @param postpendName
	 *            A name postpended to the class (null for none) name as in:
	 *            registerOnStdout(null, "STDOUT") results in a log message such
	 *            as: INFO org.mydomain.MyClass.STDOUT -Log message
	 */
	public CommonsLoggingPrintStream(final String prependName, final String postpendName) {
		this(new LoggingOutputStream(prependName, postpendName, CommonsLoggingPrintStream.class.getCanonicalName()));
	}

	private CommonsLoggingPrintStream(final LoggingOutputStream los) {
		super(los, false);
		outputStream = los;
	}

	/**
	 * Convenience method - Creates a new instance of CommonsLoggingPrintStream
	 * and registers it on STDOUT
	 *
	 * @param prependName
	 *            A name prepended to the class (null for none) name as in:
	 *            registerOnStdout("STDOUT", null) results in a log message such
	 *            as: INFO STDOUT.org.mydomain.MyClass - Log message
	 * @param postpendName
	 *            A name postpended to the class (null for none) name as in:
	 *            registerOnStdout(null, "STDOUT") results in a log message such
	 *            as: INFO org.mydomain.MyClass.STDOUT -Log message
	 * @return a reference to the CommonsLoggingPrintStream object created, can
	 *         be ignored in most situations
	 */
	public static CommonsLoggingPrintStream registerOnStdout(final String prependName, final String postpendName) {
		final CommonsLoggingPrintStream ref = new CommonsLoggingPrintStream(prependName, postpendName);
		System.setOut(ref);
		return ref;
	}

	/**
	 * Convenience method - Creates a new instance of CommonsLoggingPrintStream
	 * and registers it on STDERR
	 *
	 * @param prependName
	 *            A name prepended to the class (null for none) name as in:
	 *            registerOnStdout("STDERR", null) results in a log message such
	 *            as: INFO STDERR.org.mydomain.MyClass - Log message
	 * @param postpendName
	 *            A name postpended to the class (null for none) name as in:
	 *            registerOnStdout(null, "STDERR") results in a log message such
	 *            as: INFO org.mydomain.MyClass.STDERR -Log message
	 * @return a reference to the CommonsLoggingPrintStream object created, can
	 *         be ignored in most situations
	 */
	public static CommonsLoggingPrintStream registerOnStderr(final String prependName, final String postpendName) {
		final CommonsLoggingPrintStream ref = new CommonsLoggingPrintStream(prependName, postpendName);
		System.setErr(ref);
		return ref;
	}

	/**
	 * This class is required in order to make use of PrintWriters guarantee
	 * that flush will be called at the appropriate time. We post data to
	 * Commons Loggging only after flush() is called on the wrapped output
	 * stream by PrintWriter.
	 *
	 */
	private static class LoggingOutputStream extends ByteArrayOutputStream {
		private String currentCallerName;
		private String prependName = null;
		private String postpendName = null;
		private String outerClassName = null; // This is dynamically generated
												// so that changes to the
												// package or class name don't
												// affect functionality

		public LoggingOutputStream(final String prependName, final String postpendName, final String outerClassName) {
			this.prependName = prependName != null && !prependName.isEmpty() ? prependName + "." : "";
			this.postpendName = postpendName != null && !postpendName.isEmpty() ? "." + postpendName : "";
			this.outerClassName = outerClassName;
		}

		@Override
		public void flush() throws IOException {
			super.flush();

			// Log resulting bytes after flush() is called. We can rely on this
			// because
			// we created the PrintStream with the autoFlush option turned on.
			// If a byte array is written it may contain multiple lines
			final String[] logMessages = this.toString().split("\r\n");

			for (final String message : logMessages) {
				final String msg = message.trim();
				if (msg.length() > 0)
					LoggerFactory.getLogger(currentCallerName).info(msg);
			}
			reset();
		}

		void setNameOfCaller() {
			boolean reachedCallToOutterClass = false;
			final StackTraceElement[] stack = Thread.currentThread().getStackTrace();

			// Loop through stack trace elements until we find
			// "java.io.PrintStream"
			// and return the first fully-qualified-class-name after the calls
			// to PrintStream
			for (final StackTraceElement e : stack)
				if (e.getClassName().equals(outerClassName)) {
					reachedCallToOutterClass = true;
					continue;
				} else if (reachedCallToOutterClass) {
					currentCallerName = prependName + e.getClassName() + postpendName;
					return;
				}
			currentCallerName = "unknown.classname"; // Unreachable code (or so
														// theory holds)
		}

	}

	/**
	 * Passes the call on to outputStream.setNameOfCaller() only if the
	 * synchronized lock on this is owned once. If it's owned more than once
	 * then this is a callback from within PrintWriter, a situation which will
	 * make it difficult/impossible to determine the calling method, and is not
	 * necessary since the first call to setNameOfCaller() is all that was
	 * needed to determine the calling method.
	 */
	private void setNameOfCaller(final ReentrantLock lock) {
		if (lock.getHoldCount() > 1)
			return;
		else
			outputStream.setNameOfCaller();
	}

	/*
	 *
	 * Override all print & append methods of PrintWriter
	 *
	 * Each method adds a check for the current name of the calling method, and
	 * while still synchronized make a call that may or may not call flush.
	 *
	 * The call to flush will make use of the current name of the calling method
	 * and post the current data to commons logging
	 *
	 * The call to setNameOfCaller() must be synchronized so that the name is
	 * not lost between the set and the call to flush()
	 *
	 * Flush is sure to be called appropriately due to the PrintWriter
	 * constructor being called with autoFlush=true.
	 *
	 * The name of the calling method *must* be obtained in the PrintWriter
	 * methods, otherwise extraneous method calls in between make it impossible
	 * to determine the actual caller
	 *
	 */

	@Override
	public PrintStream append(final char c) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			return super.append(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PrintStream append(final CharSequence csq) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			return super.append(csq);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PrintStream append(final CharSequence csq, final int start, final int end) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			return super.append(csq, start, end);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final boolean b) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(b);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final char c) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final char[] s) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(s);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final double d) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(d);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final float f) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(f);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final int i) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(i);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final long l) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(l);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final Object obj) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(obj);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void print(final String s) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.print(s);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PrintStream printf(final Locale l, final String format, final Object... args) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			return super.printf(l, format, args);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PrintStream printf(final String format, final Object... args) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			return super.printf(format, args);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println() {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println();
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final boolean x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final char x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final char[] x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final double x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final float x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final int x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final long x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final Object x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void println(final String x) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.println(x);
			flush();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void write(final byte[] b) throws IOException {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.write(b);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void write(final byte[] buf, final int off, final int len) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.write(buf, off, len);
		} finally {
			lock.unlock();
		}

	}

	@Override
	public void write(final int b) {
		lock.lock();
		try {
			setNameOfCaller(lock);
			super.write(b);
		} finally {
			lock.unlock();
		}
	}

}
