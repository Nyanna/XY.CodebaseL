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

	@Override
	public void doAll(final E target, final int ms) {
		doAll(target, ms, null);
	}

	@Override
	public void doAll(final E target, final int ms, final IJobObserver<E> obs) {
		final IPerfCounter measure = getMeasure();
		if (measure != null)
			doAllMeasured(target, ms, obs, measure);
		else
			doAllPlain(target, ms, obs);
	}

	private void doAllMeasured(final E target, final int ms, final IJobObserver<E> obs, final IPerfCounter measure) {
		boolean loop = true;
		measure.stopMeasure();
		for (Runnable job = next(target, ms); job != null; job = next(target, 0)) {
			measure.startMeasure();
			if (obs == null)
				loop = runGuarded(job);
			else if (obs.startJob(target, job)) {
				loop = runGuarded(job);
				obs.endJob(target, job);
			}
			measure.stopMeasure();
			if (!loop)
				break;
		}
		measure.startMeasure();
	}

	private void doAllPlain(final E target, final int ms, final IJobObserver<E> obs) {
		boolean loop = true;
		for (Runnable job = next(target, ms); job != null; job = next(target, 0)) {
			if (obs == null)
				loop = runGuarded(job);
			else if (obs.startJob(target, job)) {
				loop = runGuarded(job);
				obs.endJob(target, job);
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

	/**
	 * @return an possibly present counter object or null
	 */
	protected IPerfCounter getMeasure() {
		return null;
	}
}
