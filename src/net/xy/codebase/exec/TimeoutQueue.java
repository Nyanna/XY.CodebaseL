package net.xy.codebase.exec;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.Primitive;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.RecurringTaskCapsule;
import net.xy.codebase.exec.tasks.TimeoutRunnable;

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
	private final PriorityQueue<ITask> queue;
	/**
	 * timer thread
	 */
	private final QueueTimer timer;

	/**
	 * default
	 */
	public TimeoutQueue(final String name) {
		queue = new PriorityQueue<ITask>(100, new TaskComparator());
		timer = new QueueTimer(this, name);
		timer.start();
	}

	/**
	 * for external provided thread
	 *
	 * @param thread
	 */
	public TimeoutQueue(final QueueTimer thread) {
		queue = new PriorityQueue<ITask>(100, new TaskComparator());
		timer = thread;
		timer.setQueue(this);
	}

	/**
	 * adds an task for scheduling
	 *
	 * @param t
	 */
	public void add(final ITask t) {
		if (LOG.isTraceEnabled())
			LOG.trace("add task [" + t + "]");

		synchronized (queue) {
			queue.add(t);
			if (queue.peek() == t)
				queue.notify();
		}
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
	 * stops proccessing
	 */
	public void shutdown() {
		timer.shutdown();
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
		 * runnign state
		 */
		private boolean running = true;

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
			final TimeoutQueue tq = this.tq;
			ITask nt = null;
			try {
				synchronized (tq.queue) {
					while (running)
						if ((nt = tq.queue.peek()) == null)
							tq.queue.wait();
						else {
							final long wns = Math.max(0, nt.nextRun() - System.nanoTime());
							final long wms = TimeUnit.NANOSECONDS.toMillis(wns);
							tq.queue.wait(wms, (int) Math.max(1, wns % 1000000));

							nt = tq.queue.peek();
							if (nt.nextRun() <= System.nanoTime())
								timedOut(nt);
						}
				}
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
			if (tq.queue.poll() != nt)
				throw new RuntimeException("head of que is not current");

			run(nt);
			if (nt.isRecurring())
				tq.queue.add(nt);
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

		/**
		 * dont proccess anymore
		 */
		public void shutdown() {
			tq.add(new TimeoutRunnable(0) {
				@Override
				public void run() {
					running = false;
				}
			});
			synchronized (tq.queue) {
				// gets freed
			}
		}
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
