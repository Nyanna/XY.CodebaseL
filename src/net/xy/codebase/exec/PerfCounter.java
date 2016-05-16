package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * performance counter implementation
 *
 * @author Xyan
 *
 */
public class PerfCounter implements IPerfCounter {
	private static final Logger LOG = LoggerFactory.getLogger(PerfCounter.class);
	/**
	 * measure startet at
	 */
	private long measureStart;
	/**
	 * timestamp of last stop call
	 */
	private long measureStop;
	/**
	 * sum of measure for average
	 */
	private long measureSum;

	/**
	 * last loop time
	 */
	private long lastLoopSum;
	/**
	 * last interval time
	 */
	private long currentInterval;
	/**
	 * last loop time
	 */
	private long lastLoopEnd;
	/**
	 * sum values
	 */
	private double overallSum;
	/**
	 * sum values
	 */
	private double intvalSum;
	/**
	 * numer of measures for sum
	 */
	private double loopCounts;
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
	public void startMeasure() {
		if (measureStart != 0) {
			measureStart = 0;
			LOG.error("Error started measure twice, ignore last measure");
		}
		measureStart = System.nanoTime();
	}

	@Override
	public void stopMeasure() {
		stopMeasure(System.nanoTime());
	}

	private void stopMeasure(final long nanoTime) {
		if (measureStart != 0) {
			measureSum += nanoTime - measureStart;
			measureStart = 0;
			measureStop = nanoTime;
		}
	}

	@Override
	public void endLoop() {
		final long now = System.nanoTime();
		stopMeasure(now);

		lastLoopSum = measureSum;
		measureSum = 0;

		intvalSum *= frame;
		overallSum *= frame;
		loopCounts *= frame;

		if (lastLoopEnd > 0) {
			currentInterval = now - lastLoopEnd;
			intvalSum += currentInterval;
		}
		lastLoopEnd = now;

		overallSum += lastLoopSum;
		loopCounts++;
	}

	@Override
	public long getLastLoopTime() {
		return TimeUnit.NANOSECONDS.toMicros(lastLoopSum);
	}

	@Override
	public long getLastIntervall() {
		return TimeUnit.NANOSECONDS.toMicros(currentInterval);
	}

	@Override
	public long getAvrLoopTime() {
		return loopCounts > 0 ? TimeUnit.NANOSECONDS.toMicros((long) (overallSum / loopCounts)) : 0l;
	}

	@Override
	public long getAvrLoopIntervalTime() {
		return loopCounts > 0 ? TimeUnit.NANOSECONDS.toMicros((long) (intvalSum / loopCounts)) : 0l;
	}

	@Override
	public long lastUpdate() {
		return TimeUnit.NANOSECONDS.toMicros(measureStop);
	}

	@Override
	public long lastUpdateAge(final long nanoTime) {
		return TimeUnit.NANOSECONDS.toMicros(nanoTime - measureStop);
	}

	@Override
	public String toString() {
		return getAvrLoopTime() + " avr " + getLastLoopTime() + " mc";
	}
}
