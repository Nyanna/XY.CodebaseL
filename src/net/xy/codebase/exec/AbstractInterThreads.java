package net.xy.codebase.exec;

/**
 * implementation for inter thread job execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum of possible threads
 */
public abstract class AbstractInterThreads<E extends Enum<E>> implements IInterThreads<E> {

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
		measure.stopMeasure();
		for (Runnable job = next(target, ms); job != null; job = next(target, 0)) {
			measure.startMeasure();
			if (obs == null)
				job.run();
			else if (obs.startJob(target, job)) {
				job.run();
				obs.endJob(target, job);
			}
			measure.stopMeasure();
		}
		measure.startMeasure();
	}

	private void doAllPlain(final E target, final int ms, final IJobObserver<E> obs) {
		for (Runnable job = next(target, ms); job != null; job = next(target, 0))
			if (obs == null)
				job.run();
			else if (obs.startJob(target, job)) {
				job.run();
				obs.endJob(target, job);
			}
	}

	/**
	 * @return an possibly present counter object or null
	 */
	protected IPerfCounter getMeasure() {
		return null;
	}
}
