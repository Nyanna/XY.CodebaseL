package net.xy.codebase.thread;

import java.util.concurrent.TimeUnit;

/**
 * performance counter implementation
 *
 * @author Xyan
 *
 */
public class PerfCounter implements IPerfCounter {
	/**
	 * last loop time
	 */
	private long lastLoop;
	/**
	 * last loop time
	 */
	private long lastInterval;
	/**
	 * last loop time
	 */
	private long lastEndLoop;
	/**
	 * measure startet at
	 */
	private long measureStart;
	/**
	 * sum of measure for average
	 */
	private long measureSum;
	/**
	 * sum values
	 */
	private double sum;
	/**
	 * sum values
	 */
	private double intvalSum;
	/**
	 * numer of measures for sum
	 */
	private double count;
	/**
	 * exponential frame for average
	 */
	private final double frame;

	/**
	 * @param decay
	 *            exponential frame
	 */
	public PerfCounter(final double decay) {
		frame = 1d - decay;
	}

	@Override
	public long getLastLoopTime() {
		return TimeUnit.NANOSECONDS.toMicros(lastLoop);
	}

	@Override
	public long getLastIntervall() {
		return TimeUnit.NANOSECONDS.toMicros(lastInterval);
	}

	@Override
	public long getAvrLoopTime() {
		return count > 0 ? TimeUnit.NANOSECONDS.toMicros((long) (sum / count)) : 0l;
	}

	@Override
	public long getAvrLoopIntervalTime() {
		return count > 0 ? TimeUnit.NANOSECONDS.toMicros((long) (intvalSum / (count + 1))) : 0l;
	}

	@Override
	public void startLoop() {
		startMeasure();
	}

	@Override
	public void startMeasure() {
		if (measureStart != 0) {
			measureStart = 0;
			throw new IllegalStateException("Started measure twice");
		}
		measureStart = System.nanoTime();
	}

	@Override
	public void stopMeasure() {
		if (measureStart > 0) {
			measureSum += System.nanoTime() - measureStart;
			measureStart = 0;
		}
	}

	@Override
	public void endLoop() {
		stopMeasure();
		lastLoop = measureSum;
		measureSum = 0;

		intvalSum *= frame;
		sum *= frame;
		count *= frame;

		final long now = System.nanoTime();
		lastInterval = now - lastEndLoop;
		if (lastInterval != now)
			intvalSum += lastInterval;
		lastEndLoop = now;

		sum += lastLoop;
		count++;
	}

	@Override
	public String toString() {
		return getAvrLoopTime() + " avr " + getLastLoopTime() + " mc";
	}
}
