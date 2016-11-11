package net.xy.codebase.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ThreadUtils.class);

	public static void sleep(final long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (final InterruptedException e) {
			LOG.info("Thread interupted", e);
		}
	}

	public static void assertThread(final String name) {
		assert((ThreadExtended) Thread.currentThread()).getThreadName().equals(name);
	}
}
