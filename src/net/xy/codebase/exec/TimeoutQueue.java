package net.xy.codebase.exec;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.Primitive;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.RecurringTaskCapsule;

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
	private IQueueObserver obs;

	/**
	 * default
	 */
	public TimeoutQueue(final String name) {
		queue = new PriorityQueue<ITask>(100, new TaskComparator());
		timer = new QueueTimer(this, name);
		timer.start();
		if (LOG.isDebugEnabled())
			LOG.debug("Created named TimeOutQueue [" + name + "][" + timer.getName() + "]");
		addDiagnosticTask();
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
		if (LOG.isDebugEnabled())
			LOG.debug("Created unnamed TimeOutQueue [" + thread + "][" + timer.getName() + "]");
		addDiagnosticTask();
	}

	public void setObserver(final IQueueObserver obs) {
		this.obs = obs;
		timer.setObserver(obs);
	}

	private void addDiagnosticTask() {
		add(30 * 1000, new Runnable() {
			@Override
			public void run() {
				if (LOG.isDebugEnabled()) {
					LOG.debug("TQueue size [" + queue.size() + "][exec=" + timer.getExecCount() + "][" + timer.getName()
							+ "]");
					timer.resetExecCount();
				}
			}
		});
	}

	/**
	 * adds an task for scheduling
	 *
	 * @param t
	 */
	public boolean add(final ITask t) {
		boolean res = false;
		if (LOG.isTraceEnabled())
			LOG.trace("add task [" + t + "]");

		synchronized (queue) {
			res = queue.add(t);
			if (queue.peek() == t)
				queue.notify();
		}
		if (!res)
			LOG.error("Error inserting task into timeout que [" + t + "][" + timer.getName() + "]");
		else if (obs != null)
			obs.taskAdded(t);
		return res;
	}

	/**
	 * convenience mehtod supporting lamda syntax for creating recurring tasks
	 *
	 * @param intervall
	 * @param run
	 */
	public RecurringTaskCapsule add(final int intervall, final Runnable run) {
		final RecurringTaskCapsule cap = new RecurringTaskCapsule(intervall, run);
		return add(cap) ? cap : null;
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
	public static class QueueTimer extends ThreadExtended {
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
		 * amount of executed tasks
		 */
		private int execCount = 0;
		/**
		 * task observer
		 */
		private IQueueObserver obs;

		/**
		 * to get post initialized by extern queue
		 *
		 * @param name
		 */
		public QueueTimer(final String name) {
			this(null, name);
		}

		public void setObserver(final IQueueObserver obs) {
			this.obs = obs;
		}

		public void resetExecCount() {
			execCount = 0;
		}

		public int getExecCount() {
			return execCount;
		}

		/**
		 * default
		 *
		 * @param queue
		 * @param name
		 */
		public QueueTimer(final TimeoutQueue queue, final String name) {
			super(name + " " + TimeoutQueue.class.getSimpleName() + "-" + ++COUNTER, true);
			setQueue(queue);
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
			final PriorityQueue<ITask> tq = this.tq.queue;
			ITask nt = null;
			try {
				while (running) {
					long wns = 0;
					synchronized (tq) {
						if ((nt = tq.peek()) == null) {
							tq.wait();
							continue;
						}

						wns = nt.nextRun() - System.nanoTime();
						if (wns > 0l) {
							final long wms = TimeUnit.NANOSECONDS.toMillis(wns);
							final int wmn = (int) (wns % 1000000);
							tq.wait(wms, wmn);
							continue;
						} else
							removeHead(nt);
					}
					timedOut(nt, wns);
				}
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * remove head elements
		 *
		 * @param nt
		 */
		private void removeHead(final ITask nt) {
			if (tq.queue.poll() != nt)
				throw new RuntimeException("head of que is not current");
		}

		/**
		 * check and try to run the next task
		 *
		 * @param nt
		 * @param wns
		 * @return null on success or the new queue head
		 */
		private void timedOut(final ITask nt, final long wns) {
			execCount++;
			run(nt, wns);
			if (nt.isRecurring())
				synchronized (tq.queue) {
					tq.queue.add(nt);
				}
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
			synchronized (tq.queue) {
				running = false;
				tq.queue.notify();
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

	/**
	 * observer interface for timeout queue
	 *
	 * @author Xyan
	 *
	 */
	public static interface IQueueObserver {

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
	}
}
