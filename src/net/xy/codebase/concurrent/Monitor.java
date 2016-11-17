package net.xy.codebase.concurrent;

/**
 * 1. dynamic slot creation and growth<br>
 * 2. thread data only hold on stack <br>
 * 3. ordered wait and wake, clear every wake permit <br>
 * 4. atomic enqueue/dequeue operations <br>
 *
 * @author Xyan
 *
 */
public class Monitor extends Sync {
	/**
	 * increments state and wakes up all waiting threads
	 *
	 * @return
	 */
	@Override
	public boolean call() {
		modCounter.incrementAndGet();
		return wakeAll(tail.get());
	}
}
