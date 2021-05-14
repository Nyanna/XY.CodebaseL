package net.xy.codebase.exec.tq;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.concurrent.Semaphore;
import net.xy.codebase.exec.ThreadExtended;
import net.xy.codebase.exec.ThreadUtils;
import net.xy.codebase.exec.tasks.ITask;

/**
 * timer thread doing the work
 *
 * @author Xyan
 *
 */
public class QueueTimerThread extends ThreadExtended {
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutQueue.class);
	/**
	 * counter for threadname numbering
	 */
	public static AtomicInteger COUNTER = new AtomicInteger();
	/**
	 * ref to parent queue
	 */
	private PriorityBlockingQueue<ITask> queue;
	/**
	 * back reference to the surrounding TimeoutQueue for task add purpose
	 */
	private TimeoutQueue timeoutQueue;
	/**
	 * for waits in empty ques
	 */
	private Semaphore added;
	/**
	 * runnign state
	 */
	private boolean running = true;
	/**
	 * ts of last shutdown call
	 */
	private long stopedAt;
	/**
	 * observer
	 */
	private IQueueObserver obs;

	/**
	 * to get post initialized by extern queue
	 *
	 * @param name
	 */
	public QueueTimerThread(final String name) {
		super(name + " " + TimeoutQueue.class.getSimpleName() + "-" + COUNTER.incrementAndGet(), false);
	}

	public void setObserver(final IQueueObserver obs) {
		if (this.obs != null && obs != null)
			throw new IllegalStateException("There is already an observer registered [" + this.obs + "]");
		this.obs = obs;
	}

	/**
	 * sets the waiting condition to preserve cpu
	 *
	 * @param added
	 */
	public void setCondition(final Semaphore added) {
		this.added = added;
	}

	/**
	 * relocates to antoher queue
	 *
	 * @param queue
	 */
	public void setQueue(final PriorityBlockingQueue<ITask> queue) {
		this.queue = queue;
	}

	/**
	 * set back reference for readd purpose
	 *
	 * @param timeoutQueue
	 */
	public void setTimeoutQueue(final TimeoutQueue timeoutQueue) {
		this.timeoutQueue = timeoutQueue;
	}

	@Override
	public void run() {
		final PriorityBlockingQueue<ITask> q = queue;
		ITask nt = null;
		while (isRunning())
			try {
				final int state = added.getState();
				if ((nt = q.poll()) == null) {
					added.await(state);
					continue;
				}
				nt.leaveQueue();

				final long nextRun = nt.nextRun();
				long wns = 0;
				if (nextRun > 0 && (wns = nextRun - System.nanoTime()) > 0l) {
					// first wait time to first task
					added.await(state, wns);
					// than add again to repeat
					timeoutQueue.add(nt);
					continue;
				}

				timedOut(nt, wns);
			} catch (final Exception e) {
				LOG.error("Error in TQ loop", e);
				ThreadUtils.sleep(1000);
			}
		LOG.info("Exit TimeOutQueue Timer Thread [" + getName() + "]");
		if (obs != null)
			obs.queueExited();

	}

	/**
	 * check and try to run the next task
	 *
	 * @param nt
	 * @param wns
	 * @return null on success or the new queue head
	 */
	private void timedOut(final ITask nt, final long wns) {
		run(nt, wns);
		if (!running && System.currentTimeMillis() > stopedAt + TimeUnit.SECONDS.toMillis(10))
			LOG.info("QueueTimer is shutting down and executes [" + nt + "][" + getName() + "]");
	}

	/**
	 * catches the run
	 *
	 * @param t
	 */
	private void run(final ITask t, final long wns) {
		try {
			if (obs != null)
				obs.taskStarted(t, wns);
			if (LOG.isTraceEnabled())
				LOG.trace("firing task [" + t + "]");
			t.run();
		} catch (final Exception e) {
			LOG.error("Error running task", e);
		} finally {
			if (obs != null)
				obs.taskStoped(t);
		}
	}

	/**
	 * dont proccess anymore
	 */
	public void shutdown() {
		stopedAt = System.currentTimeMillis();
		running = false;
		added.callAll();
		LOG.info("Shutdown of QueueTimer was called  [" + getName() + "]");
	}

	/**
	 * @return while loop is active
	 */
	public boolean isRunning() {
		return running || queue.size() > 0;
	}
}