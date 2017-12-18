package net.xy.codebase.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * implementation for inter thread job execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum of possible threads
 */
public abstract class AbstractInterThreads<E extends Enum<E>> implements IInterThreads<E> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractInterThreads.class);
	protected IJobObserver<E> obs = new JobObserver<E>();

	@Override
	public void setObserver(final IJobObserver<E> obs) {
		this.obs = obs;
	}

	@Override
	public IJobObserver<E> getObserver() {
		return obs;
	}

	@Override
	public void doAll(final E target, final int ms, final IPerfCounter measure) {
		boolean loop = true;
		for (Runnable job = next(target, ms); job != null; job = next(target, 0)) {
			if (obs == null)
				loop = runGuarded(job);
			else if (obs.jobStart(target, job, measure)) {
				final long start = System.nanoTime();
				loop = runGuarded(job);
				obs.jobEnd(target, job, measure, System.nanoTime() - start);
			}
			if (!loop)
				break;
		}
	}

	/**
	 * @param job
	 * @return true to procceed normaly
	 */
	private boolean runGuarded(final Runnable job) {
		try {
			job.run();
		} catch (final InterruptedException ex) {
			// clean interuped
			return false;
		} catch (final Exception e) {
			LOG.error("Error running job", e);
		}
		return true;
	}
}
