package net.xy.codebase.exec;

import net.xy.codebase.exec.TimeoutQueue.IQueueObserver;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.RecurringTask;
import net.xy.codebase.exec.tasks.TimeoutRunnable;

/**
 * interface for cross thread task execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum describing all possible target thread categories
 */
public interface IInterThreads<E extends Enum<E>> {
	/**
	 * waits up to ms for the next job
	 *
	 * @param target
	 * @param ms
	 *            -1 waits until one job gots collected, 0 to try and return
	 * @return
	 */
	public Runnable next(E target, int ms);

	/**
	 * waits up to ms for at least one job to execute up to all jobs in que are
	 * executed
	 *
	 * @param target
	 * @param ms
	 *            -1 waits until one job gots collected, 0 to try and return
	 */
	public void doAll(E target, int ms, IPerfCounter measure);

	/**
	 * put an job in this target threads category queue
	 *
	 * @param target
	 * @param job
	 * @return true on success
	 */
	public boolean run(E target, Runnable job);

	/**
	 * gets an bounded throtler for the target thread
	 *
	 * @param thread
	 * @param run
	 * @return null on failure
	 */
	public ExecutionThrottler getThrottler(E thread, Runnable run);

	/**
	 * gets an bounded throtler for the target thread with an specific minimum
	 * intervall
	 *
	 * @param thread
	 * @param run
	 * @param intervallMs
	 * @return null on failure
	 */
	public ExecutionThrottler getThrottler(E thread, Runnable run, int intervallMs);

	/**
	 * gets an bounded throtler for the target thread supporting priority
	 * execution used bei priority thread worker queues
	 *
	 * @param thread
	 * @param run
	 * @param priority
	 * @return null on failure
	 */
	public ExecutionThrottler getPriorityThrottler(E thread, Runnable run, int priority);

	/**
	 * gets an concurrency limiter for parallel execution by specific amounts
	 *
	 * @param thread
	 * @param run
	 * @param priority
	 * @param amount
	 *            of concurrent runnables
	 * @return null on failure
	 */
	public ExecutionLimiter getPriorityLimiter(E thread, Runnable run, int priority, int amount);

	/**
	 * enques an runnable for later execution
	 *
	 * @param thread
	 * @param run
	 * @param timeout
	 *            in milliseconds
	 * @return null on failure
	 */
	public TimeoutRunnable runLater(E thread, Runnable run, int timeout);

	/**
	 * starts an intervall regulary dilivering runnables to target thread
	 *
	 * @param thread
	 *            target thread category
	 * @param run
	 * @param intervall
	 *            intervall to run at in milliseconds
	 * @return null on failure
	 */
	public RecurringTask startIntervall(E thread, Runnable run, int intervall);

	/**
	 * starts an intervall regulary dilivering runnables to target thread, with
	 * an predefined start timeout
	 *
	 * @param thread
	 *            target thread category
	 * @param run
	 * @param startDelay
	 *            delay for first startup in milliseconds
	 * @param intervall
	 *            intervall to run at in milliseconds
	 * @return null on failure
	 */
	public RecurringTask startDelayed(E thread, Runnable run, int startDelay, int intervall);

	/**
	 * start self supplied recuring task
	 *
	 * @param task
	 * @return false on failure
	 */
	public boolean start(ITask task);

	public void setObserver(IJobObserver<E> obs);

	public IJobObserver<E> getObserver();

	/**
	 * interceptor interface for progress listenting
	 *
	 * @author Xyan
	 *
	 */
	public static interface IJobObserver<E> extends IQueueObserver {
		/**
		 *
		 * @param target
		 * @param job
		 * @param measure
		 * @return when false job gots skipped
		 */
		public boolean jobStart(E target, Runnable job, IPerfCounter measure);

		/**
		 * after the job gets executed
		 *
		 * @param target
		 * @param job
		 * @param measure
		 * @param duration
		 */
		public void jobEnd(E target, Runnable job, IPerfCounter measure, long duration);

		/**
		 * called when an task has to be droped
		 *
		 * @param target
		 * @param job
		 * @param size
		 */
		public void jobDroped(E target, Runnable job, int size);

		/**
		 * called when an job was added to the queue
		 * 
		 * @param target
		 * @param job
		 */
		public void jobAdded(E target, Runnable job);
	}

	/**
	 * thrown by runnables causes an clean end of task proccessing
	 *
	 * @author Xyan
	 *
	 */
	public static class InterruptedException extends RuntimeException {
		private static final long serialVersionUID = -6268270281416217298L;
	}
}
