package net.xy.codebase.collection;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.Primitive;
import net.xy.codebase.thread.RecurringTaskCapsule;

/**
 * timeout queue implementation threadsafe based on task objects which must
 * decouple itself
 *
 * @author Xyan
 *
 */
public class TimeoutQueue {
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutQueue.class);
	/**
	 * ordered task queue
	 */
	private final PriorityBlockingQueue<ITask> queue;
	/**
	 * monitor for queue head update checks
	 */
	private final Semaphore monitor;
	/**
	 * timer thread
	 */
	private final QueueTimer timer;

	/**
	 * default
	 */
	public TimeoutQueue(final String name) {
		queue = new PriorityBlockingQueue<ITask>(100, new TaskComparator());
		monitor = new Semaphore(0);
		timer = new QueueTimer(this, name);
		timer.start();
	}

	/**
	 * for external provided thread
	 *
	 * @param thread
	 */
	public TimeoutQueue(final QueueTimer thread) {
		queue = new PriorityBlockingQueue<ITask>(100, new TaskComparator());
		monitor = new Semaphore(0);
		timer = thread;
		timer.setQueue(this);
		timer.start();
	}

	/**
	 * adds an task for scheduling
	 *
	 * @param t
	 */
	public void add(final ITask t) {
		if (LOG.isTraceEnabled())
			LOG.trace("add task [" + t + "]");
		queue.add(t);
		if (queue.peek() == t)
			monitor.release();
	}

	/**
	 * convenience mehtod supporting lamda syntax for creating recurring tasks
	 *
	 * @param intervall
	 * @param run
	 */
	public RecurringTaskCapsule add(final int intervall, final Runnable run) {
		final RecurringTaskCapsule cap = new RecurringTaskCapsule(intervall, run);
		add(cap);
		return cap;
	}

	/**
	 * timer thread doing the work
	 *
	 * @author Xyan
	 *
	 */
	public static class QueueTimer extends Thread {
		/**
		 * counter for threadname numbering
		 */
		public static int COUNTER = 0;
		/**
		 * ref to parent queue
		 */
		private TimeoutQueue tq;

		/**
		 * to get post initialized by extern queue
		 *
		 * @param name
		 */
		public QueueTimer(final String name) {
			this(null, name);
		}

		/**
		 * default
		 *
		 * @param queue
		 * @param name
		 */
		public QueueTimer(final TimeoutQueue queue, final String name) {
			setQueue(queue);
			setName(name + " " + TimeoutQueue.class.getSimpleName() + "-" + ++COUNTER);
			setDaemon(true);
		}

		/**
		 * relocates to antoher queue
		 *
		 * @param tq
		 */
		public void setQueue(final TimeoutQueue tq) {
			this.tq = tq;
		}

		@Override
		public void run() {
			ITask nt = null;
			while (true)
				try {
					final TimeoutQueue tq = this.tq;
					nt = tq.queue.peek();
					if (nt == null) {
						tq.monitor.acquire();
						continue;
					}

					final long waitTime = nt.nextRun() - System.currentTimeMillis();
					if (!tq.monitor.tryAcquire(waitTime, TimeUnit.MILLISECONDS))
						timedOut(nt);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
		}

		/**
		 * check and try to run the next task
		 *
		 * @param nt
		 * @return null on success or the new queue head
		 */
		private void timedOut(final ITask nt) {
			final ITask t = tq.queue.poll();
			run(t);
			if (t.isRecurring())
				tq.add(t);
		}

		/**
		 * catches the run
		 *
		 * @param t
		 */
		private void run(final ITask t) {
			try {
				if (LOG.isTraceEnabled())
					LOG.trace("firing task [" + t + "]");
				t.run();
			} catch (final Exception e) {
				LOG.error("Error running task", e);
			}
		}
	}

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
		 * @return time to run this task at next
		 */
		public long nextRun();
	}

	/**
	 * comparator for task ordering
	 *
	 * @author Xyan
	 *
	 */
	public static class TaskComparator implements Comparator<ITask> {
		@Override
		public int compare(final ITask t1, final ITask t2) {
			return Primitive.compare(t1.nextRun(), t2.nextRun());
		}
	}
}
