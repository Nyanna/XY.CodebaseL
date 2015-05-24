package net.xy.codebase.collection;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * timeout queue implementation threadsafe based on task objects which must
 * decouple itself
 *
 * @author Xyan
 *
 */
public class TimeOutQueue {
	/**
	 * ordered task queue
	 */
	private final PriorityBlockingQueue<Task> queue;
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
	public TimeOutQueue() {
		queue = new PriorityBlockingQueue<>(100, new TaskComparator());
		monitor = new Semaphore(0);
		timer = new QueueTimer(this);
		timer.start();
	}

	/**
	 * adds an task for scheduling
	 *
	 * @param t
	 */
	public void add(final Task t) {
		queue.add(t);
		if (queue.peek() == t)
			monitor.release();
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
		private final TimeOutQueue tq;

		/**
		 * default
		 *
		 * @param queue
		 */
		public QueueTimer(final TimeOutQueue queue) {
			tq = queue;
			setName(TimeOutQueue.class.getName() + ++COUNTER);
			setDaemon(true);
		}

		@Override
		public void run() {
			Task nt = null;
			while (true)
				try {
					if (nt == null)
						nt = tq.queue.peek();
					final long waitTime = getWaitTime(nt);
					if (waitTime > 0)
						tq.monitor.tryAcquire(waitTime, TimeUnit.MILLISECONDS);
					else if (waitTime == 0)
						tq.monitor.acquire();

					nt = runNext(nt);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
		}

		/**
		 * check and try to run the next tast
		 *
		 * @param nt
		 * @return null on success or the new qeue head
		 */
		private Task runNext(final Task nt) {
			final Task t = tq.queue.peek();
			if (t != nt)
				return t;

			if (t.isRecurring())
				tq.add(t);
			t.run();
			return null;
		}

		/**
		 * calculate time tor wait for next tast execution
		 *
		 * @param t
		 * @return
		 */
		private long getWaitTime(final Task t) {
			if (t == null)
				return 0;

			final long nextRun = t.nextRun();
			final long wait = nextRun - System.currentTimeMillis();
			if (wait <= 0)
				return -1;
			return wait;
		}
	}

	/**
	 * task contract
	 *
	 * @author Xyan
	 *
	 */
	public interface Task extends Runnable {

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
	public static class TaskComparator implements Comparator<Task> {
		@Override
		public int compare(final Task t1, final Task t2) {
			return Long.compare(t1.nextRun(), t2.nextRun());
		}
	}
}
