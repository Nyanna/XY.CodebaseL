package net.xy.codebase.exec.tasks;

/**
 * interface for real action and scheduler
 *
 * @author Xyan
 *
 */
public interface IScheduleRunnable extends Runnable {
	/**
	 * action should now to schedule his own capsule
	 *
	 * @param run
	 * @param delay
	 * @return false on failure
	 */
	public boolean schedule(ITask run);
}