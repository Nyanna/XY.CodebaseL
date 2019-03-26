package net.xy.codebase.exec.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.Semaphore;
import net.xy.codebase.exec.IInterThreads.InterruptedException;

public class Worker implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	/**
	 * backreference to executor
	 */
	private final ITaskSource executor;

	public Worker(final ITaskSource executor) {
		this.executor = executor;
	}

	@Override
	public void run() {
		final Semaphore added = executor.getCondition();
		while (true) {
			final int state = added.getState(); // for null job
			if (!executor.next(this))
				return;
			added.await(state); // runs when job was not initially null
		}
	}

	/**
	 * @param job
	 * @return true to procceed normaly
	 */
	public boolean run(final Runnable job) {
		try {
			job.run();
		} catch (final InterruptedException ex) {
			// clean interuped
			return false;
		} catch (final Exception e) {
			LOG.error("Error running job [" + job + "][" + Thread.currentThread().getName() + "]", e);
		} finally {
			// loop again if i got a job
			executor.getCondition().call();
		}
		return true;
	}
}
