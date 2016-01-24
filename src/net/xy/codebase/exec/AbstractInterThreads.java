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
		// causes mem alloc through synchronizer node
		final IPerfCounter measure = getMeasure();

		if (measure != null)
			measure.stopMeasure();

		for (Runnable job = next(target); job != null; job = next(target)) {
			if (measure != null)
				measure.startMeasure();

			job.run();

			if (measure != null)
				measure.stopMeasure();
		}

		if (measure != null)
			measure.startMeasure();
	}

	@Override
	public void doAll(final E target, final int ms) {
		// causes mem alloc through synchronizer node
		final IPerfCounter measure = getMeasure();

		if (measure != null)
			measure.stopMeasure();

		for (Runnable job = next(target, ms); job != null; job = next(target)) {
			if (measure != null)
				measure.startMeasure();

			// final long start = System.currentTimeMillis();
			job.run();
			// final long tok = System.currentTimeMillis() - start;
			// if (Thread.currentThread().getName().equals("Game") && tok > 2)
			// System.out.println("Running job [" + start + "][" + tok + "][" +
			// job + "]");

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
