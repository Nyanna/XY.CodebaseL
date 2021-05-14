package net.xy.codebase.exec;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
	private final AtomicLong measureStop = new AtomicLong();
	/**
	 * sum of measure for average
	 */
	private final AtomicLong measureSum = new AtomicLong();
	/**
	 * time when the loop last time was force to stop in multithreding mode
	 */
	private final AtomicLong lastLoopStoped = new AtomicLong();

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
	 * amount of start calls in current loop
	 */
	private long loopCalls;
	/**
	 * amount of start calls in last loop
	 */
	private long lastLoopCalls;
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
	 * @param decay exponential frame
	 */
	public PerfCounter(final double decay) {
		frame = 1d - decay;
	}

	@Override
	public void startMeasure() {
		startMeasure(System.nanoTime());
	}

	@Override
	public void startMeasure(final long nanoTime) {
		if (measureStart != 0) {
			measureStart = 0;
			LOG.error("Error started measure twice, ignore last measure", new Exception());
		}
		measureStart = nanoTime;
		loopCalls++;
	}

	@Override
	public void stopMeasure() {
		stopMeasure(System.nanoTime());
	}

	@Override
	public void stopMeasure(final long nanoTime) {
		if (measureStart != 0) {
			measureSum.addAndGet(nanoTime - measureStart);
			measureStart = 0;
			measureStop.set(nanoTime);
		}
	}

	/**
	 * for multithreading mode
	 */
	@Override
	public void countMeasure(final long nanoTime, final long measureStart) {
		measureSum.addAndGet(nanoTime - measureStart);
		measureStop.set(nanoTime);

		final long lastLoop = lastLoopStoped.get();
		if (lastLoop < nanoTime - 100000000 && lastLoopStoped.compareAndSet(lastLoop, nanoTime)) // 100ms
			endLoop(nanoTime);
	}

	@Override
	public void endLoop() {
		endLoop(System.nanoTime());
	}

	@Override
	public void endLoop(final long nanoTime) {
		stopMeasure(nanoTime);

		lastLoopSum = measureSum.getAndSet(0);

		intvalSum *= frame;
		overallSum *= frame;
		loopCounts *= frame;

		if (lastLoopEnd > 0) {
			currentInterval = nanoTime - lastLoopEnd;
			intvalSum += currentInterval;
		}
		lastLoopEnd = nanoTime;

		overallSum += lastLoopSum;
		loopCounts++;

		lastLoopCalls = loopCalls;
		loopCalls = 0;
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
	public long getIdleTime() {
		return TimeUnit.NANOSECONDS.toMicros(currentInterval - lastLoopSum);
	}

	@Override
	public float getUseFraction() {
		return lastLoopSum / (float) currentInterval;
	}

	@Override
	public float getUseAvrFraction() {
		return (float) (overallSum / loopCounts / (intvalSum / loopCounts));
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
		return TimeUnit.NANOSECONDS.toMicros(measureStop.get());
	}

	@Override
	public long lastUpdateAge(final long nanoTime) {
		return TimeUnit.NANOSECONDS.toMicros(nanoTime - measureStop.get());
	}

	@Override
	public long getLastLoopCalls() {
		return lastLoopCalls;
	}

	@Override
	public String toString() {
		return getAvrLoopTime() + " avr " + getLastLoopTime() + " mc";
	}
}
