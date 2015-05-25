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
	 * measure startet at
	 */
	private long measureStart;
	/**
	 * sum of measure for average
	 */
	private long measureSum;
	/**
	 * average values
	 */
	private int avr;
	/**
	 * numer of measures for avr
	 */
	private int count = 1;
	/**
	 * amount of measures to target the average
	 */
	private final int frame = 180;

	@Override
	public long getLastLoopTime() {
		return TimeUnit.NANOSECONDS.toMicros(lastLoop);
	}

	@Override
	public long getAvrLoopTime() {
		if (measureStart > System.nanoTime() - TimeUnit.SECONDS.toNanos(1))
			return TimeUnit.NANOSECONDS.toMicros(avr);
		else
			return 0l;
	}

	@Override
	public void startLoop() {
		startMeasure();
	}

	@Override
	public void startMeasure() {
		measureStart = System.nanoTime();
	}

	@Override
	public void stopMeasure() {
		measureSum += System.nanoTime() - measureStart;
	}

	@Override
	public void endLoop() {
		stopMeasure();
		lastLoop = measureSum;
		measureSum = 0;
		int frame = this.frame;
		if (count < frame)
			frame = count++;
		avr = (int) ((avr * frame + lastLoop) / (frame + 1));
	}
}
