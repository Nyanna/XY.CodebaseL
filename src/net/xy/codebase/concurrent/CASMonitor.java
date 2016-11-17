package net.xy.codebase.concurrent;

/**
 * 1. every thread handles it queue node self <br>
 * 1b. that allows pooling of nodes <br>
 * 1c. stale nodes in other threads only cause wake <br>
 * 2. ordered wait and wake, clear every wake permit <br>
 * 3. every thread only leaves on timeout or state change <br>
 * 4. atomic enqueue/dequeue operations <br>
 * 5. state change ensures validness on queue and will loop at least once <br>
 * 5b. due next field will only be set on enqueue <br>
 *
 * @author Xyan
 *
 */
public class CASMonitor extends CASSync {
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
