package net.xy.codebase.exec.tasks;

/**
 * task contract
 *
 * @author Xyan
 *
 */
public interface ITask extends Runnable {

	/**
	 * @return when this job should automaticly readded for further
	 *         execution
	 */
	public boolean isRecurring();

	/**
	 * @return time to run this task at next in nanotime
	 */
	public long nextRun();
}