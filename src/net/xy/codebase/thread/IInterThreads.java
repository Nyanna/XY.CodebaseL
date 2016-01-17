package net.xy.codebase.thread;

import net.xy.codebase.collection.TimeoutQueue.ITask;
import net.xy.codebase.exec.ExecutionThrottler;
import net.xy.codebase.exec.TimeoutRunnable;

/**
 * interface for cross thread execution factories
 *
 * @author Xyan
 *
 * @param <E>
 *            enum describing all possible target threads
 */
public interface IInterThreads<E extends Enum<E>> {

	/**
	 * @param target
	 * @return the next job or null, returns instantly
	 */
	public Runnable next(E target);

	/**
	 * waits up to ms for the next job
	 *
	 * @param target
	 * @param ms
	 * @return
	 */
	public Runnable next(E target, int ms);

	/**
	 * put an job in this target threads queue
	 *
	 * @param target
	 * @param job
	 */
	public void put(E target, Runnable job);

	/**
	 * do all currently queued jobs
	 *
	 * @param target
	 */
	public void doAll(E target);

	/**
	 * waits up to ms for at least one job to execute
	 *
	 * @param target
	 * @param ms
	 */
	public void doAll(E target, int ms);

	/**
	 * gets an bounded throtler for the target thread
	 *
	 * @param thread
	 * @param run
	 * @return
	 */
	public ExecutionThrottler getThrottler(E thread, Runnable run);

	/**
	 * gets an bounded throtler for the target thread at an specific intervall
	 *
	 * @param thread
	 * @param run
	 * @param intervallMs
	 * @return
	 */
	public ExecutionThrottler getThrottler(E thread, Runnable run, int intervallMs);

	/**
	 * enques an runnable for later execution
	 *
	 * @param thread
	 * @param run
	 * @param timeout
	 * @return
	 */
	public TimeoutRunnable runLater(E thread, Runnable run, int timeout);

	/**
	 * starts an intervall regulary dilivering runnables to target thread
	 *
	 * @param thread
	 * @param run
	 * @param intervall
	 * @return
	 */
	public RecurringTask start(E thread, Runnable run, int intervall);

	/**
	 * start self supplied recuring task
	 *
	 * @param task
	 */
	public void start(ITask task);
}
