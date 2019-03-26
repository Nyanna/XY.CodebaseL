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
	 * for recurring implementation have to self readd
	 *
	 * @param tq
	 */
	public void setQueue(TimeoutQueue tq);

	/**
	 * an fixed ts created uppon tq insertion to secure binary heap
	 *
	 * @param executed
	 */
	public long nextRunFixed();
}