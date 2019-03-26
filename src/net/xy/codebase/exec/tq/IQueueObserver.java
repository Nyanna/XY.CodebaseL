package net.xy.codebase.exec.tq;

import net.xy.codebase.exec.tasks.ITask;

/**
 * observer interface for timeout queue
 *
 * @author Xyan
 *
 */
public interface IQueueObserver {

	/**
	 * task was added to que
	 *
	 * @param t
	 */
	public void taskAdded(ITask t);

	/**
	 * task was started maybe with a letency
	 *
	 * @param t
	 * @param latency
	 */
	public void taskStarted(ITask t, long latency);

	/**
	 * task finished and returned, also exeptionally
	 *
	 * @param t
	 */
	public void taskStoped(ITask t);

	/**
	 * listens for queue shutdown
	 */
	public void queueExited();
}