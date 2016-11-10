package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadMonitor {
	/**
	 * amount of threads in object active
	 */
	protected AtomicInteger enterCount = new AtomicInteger();

	/**
	 * an thread enters to the monitor range
	 */
	public void enter() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0 && enterCount.compareAndSet(entered, entered + 1))
				break;
			Thread.yield();
		}
	}

	/**
	 * an thread leaves the monitors range
	 */
	public void leave() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0 && enterCount.compareAndSet(entered, entered - 1))
				break;
			else if (entered < 0 && enterCount.compareAndSet(entered, entered + 1))
				break;
			Thread.yield();
		}
	}

	/**
	 * amount of threads current in monitor range
	 *
	 * @return
	 */
	public int count() {
		return Math.abs(enterCount.get());
	}

	/**
	 * locks entering
	 */
	public void lock() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered < 0)
				throw new IllegalMonitorStateException("Already locked");
			if (enterCount.compareAndSet(entered, -entered))
				break;
			Thread.yield();
		}
	}

	/**
	 * waits untils given amount of threads left in block
	 *
	 * @param count
	 */
	public void wait(final int count) {
		lock();
		for (;;) {
			if (enterCount.compareAndSet(-count, -count))
				break;
			Thread.yield();
		}
	}

	/**
	 * frees lock
	 */
	public void release() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0)
				throw new IllegalMonitorStateException("Not locked");
			if (enterCount.compareAndSet(entered, -entered))
				break;
			Thread.yield();
		}
	}
}
