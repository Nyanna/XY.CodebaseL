package net.xy.codebase.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

import net.xy.codebase.exec.ThreadUtils;

/**
 * lock independent monitor build on CAS, uses spinlocks for blocking and waits
 *
 * @author Xyan
 *
 */
public class ThreadMonitor {
	/**
	 * amount of threads in object active, -N - 1 is marker for locked state
	 */
	protected AtomicInteger enterCount = new AtomicInteger();

	/**
	 * an thread enters to the monitor range
	 */
	public void enter() {
		int loop = 0;
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0 && enterCount.compareAndSet(entered, entered + 1))
				break;
			loop = ThreadUtils.yieldCAS(loop);
		}
	}

	/**
	 * tries to enter in spin loop or gives up
	 *
	 * @param waitTime
	 */
	public boolean tryEnter(final long waitTime) {
		final long start = System.nanoTime();
		int loop = 0;
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0 && enterCount.compareAndSet(entered, entered + 1))
				break;

			if (waitTime >= 0 && System.nanoTime() - start >= waitTime)
				return false;
			loop = ThreadUtils.yieldCAS(loop);
		}
		return true;
	}

	/**
	 * an thread leaves the monitors range
	 */
	public void leave() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0 && enterCount.compareAndSet(entered, entered - 1))
				break;
			else if (entered == -1)
				throw new IllegalStateException("Leave but not entered ?");
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
		final int count = enterCount.get();
		if (count >= 0)
			return count;
		return Math.abs(count + 1);
	}

	/**
	 * locks entering
	 */
	public void lock() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered < 0)
				throw new IllegalMonitorStateException("Already locked [" + enterCount.get() + "]");
			if (enterCount.compareAndSet(entered, -entered - 1))
				break;
			Thread.yield();
		}
	}

	/**
	 * locks and waits untils this block thread count is same as given block
	 * thread count
	 *
	 * @param thc
	 */
	public void lockwait(final ThreadMonitor thc) {
		lock();
		wait(thc);
	}

	public void wait(final ThreadMonitor thc) {
		int loop = 0;
		for (;;) {
			if (enterCount.get() >= 0)
				throw new IllegalMonitorStateException("Not locked [" + enterCount.get() + "]");
			final int count = thc.count();
			if (enterCount.compareAndSet(-count - 1, -count - 1))
				break;
			loop = ThreadUtils.yieldCAS(loop);
		}
	}

	/**
	 * locks and waits until given absolute count of threads is in block
	 *
	 * @param count
	 */
	public void lockwaitAbs(final int count) {
		lock();
		waitAbs(count);
	}

	/**
	 * absolute count variant of waiting
	 * 
	 * @param count
	 */
	public void waitAbs(final int count) {
		int loop = 0;
		for (;;) {
			if (enterCount.get() >= 0)
				throw new IllegalMonitorStateException("Not locked [" + enterCount.get() + "]");
			if (enterCount.compareAndSet(-count - 1, -count - 1))
				break;
			loop = ThreadUtils.yieldCAS(loop);
		}
	}

	/**
	 * frees lock
	 */
	public void release() {
		for (;;) {
			final int entered = enterCount.get();
			if (entered >= 0)
				throw new IllegalMonitorStateException("Not locked [" + enterCount.get() + "]");
			if (enterCount.compareAndSet(entered, -entered - 1))
				break;
			Thread.yield();
		}
	}
}
