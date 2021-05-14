package net.xy.codebase.exec.tasks;

import net.xy.codebase.exec.tq.TimeoutQueue;

/**
 * task contract
 *
 * @author Xyan
 *
 */
public interface ITask extends Runnable {
	/**
	 * @return time to run this task at next in nanotime
	 */
	public long nextRun();

	/**
	 * for recurring implementation have to self read, also acts as a boundary check
	 * for double insertions or removals
	 *
	 * @param tq
	 */
	public void enterQueue(TimeoutQueue tq);

	/**
	 * acts as a boundary check for double insertions or removals
	 */
	public void leaveQueue();

	/**
	 * an fixed ts created uppon tq insertion to secure binary heap
	 *
	 * @param executed
	 */
	public long nextRunFixed();
}