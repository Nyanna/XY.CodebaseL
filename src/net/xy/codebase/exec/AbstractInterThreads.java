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
	public void doAll(final E target) {
		doAll(target, null);
	}

	@Override
	public void doAll(final E target, final IJobObserver<E> obs) {
		// causes mem alloc through synchronizer node
		final IPerfCounter measure = getMeasure();

		if (measure != null)
			measure.stopMeasure();

		for (Runnable job = next(target); job != null; job = next(target)) {
			if (measure != null)
				measure.startMeasure();
			boolean runJob = true;
			if (obs != null)
				runJob = obs.startJob(target, job);

			if (runJob) {
				job.run();

				if (obs != null)
					obs.endJob(target, job);
			}
			if (measure != null)
				measure.stopMeasure();
		}

		if (measure != null)
			measure.startMeasure();
	}

	@Override
	public void doAll(final E target, final int ms) {
		doAll(target, ms, null);
	}

	@Override
	public void doAll(final E target, final int ms, final IJobObserver<E> obs) {
		// causes mem alloc through synchronizer node
		final IPerfCounter measure = getMeasure();

		if (measure != null)
			measure.stopMeasure();

		for (Runnable job = next(target, ms); job != null; job = next(target)) {
			if (measure != null)
				measure.startMeasure();
			boolean runJob = true;
			if (obs != null)
				runJob = obs.startJob(target, job);

			if (runJob) {
				job.run();

				if (obs != null)
					obs.endJob(target, job);
			}
			if (measure != null)
				measure.stopMeasure();
		}

		if (measure != null)
			measure.startMeasure();
	}

	/**
	 * @return an possibly present counter object or null
	 */
	protected IPerfCounter getMeasure() {
		return null;
	}
}
