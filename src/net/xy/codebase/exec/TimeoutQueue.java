package net.xy.codebase.exec;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.Primitive;
import net.xy.codebase.concurrent.Monitor;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.ScheduledTask;
import net.xy.codebase.exec.tasks.ScheduledTaskAdapter;

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
	 * timer thread
	 */
	private final QueueTimer timer;
	/**
	 * observe queue task processing
	 */
	private IQueueTaskObserver obs;
	/**
	 * observes tq lifecycle
	 */
	private IQueueObserver qobs;
	/**
	 * diagnostics timer
	 */
	private ScheduledTask diagnostic;
	/**
	 * for waits in empty ques
	 */
	private final Monitor isFilled = new Monitor();

	/**
	 * default
	 */
	public TimeoutQueue(final String name) {
		this(new QueueTimer(name));
	}

	/**
	 * for external provided thread
	 *
	 * @param thread
	 */
	public TimeoutQueue(final QueueTimer thread) {
		queue = new PriorityBlockingQueue<ITask>(100, new TaskComparator());
		timer = thread;
		timer.setQueue(this);
		timer.setPriority(Thread.MAX_PRIORITY);
		if (!timer.isAlive())
			timer.start();
		if (LOG.isDebugEnabled())
			LOG.debug("Created TimeOutQueue [" + thread + "][" + timer.getName() + "]");
		addDiagnosticTask();
	}

	public void setObserver(final IQueueTaskObserver obs) {
		if (this.obs != null && obs != null)
			throw new IllegalStateException("There is already an observer registered [" + this.obs + "]");
		this.obs = obs;
	}

	public void setQueueObserver(final IQueueObserver qobs) {
		if (this.qobs != null && qobs != null)
			throw new IllegalStateException("There is already an queue observer registered [" + this.qobs + "]");
		this.qobs = qobs;
		timer.setQueueObserver(qobs);
	}

	private void addDiagnosticTask() {
		add(diagnostic = new ScheduledTask(30 * 1000) {
			private long lastRun = System.currentTimeMillis();

			@Override
			public void innerRun() {
				final long now = System.currentTimeMillis();
				if (LOG.isDebugEnabled()) {
					LOG.debug("TQueue size [" + queue.size() + "][exec=" + timer.getExecCount() + "][" + timer.getName()
							+ "]");

					final long deltaT = now - lastRun;
					final float tps = (float) timer.getExecCount() / deltaT * 1000;
					LOG.debug("TQueue avr [" + tps + "][exec=" + timer.getExecCount() + "][delta=" + deltaT + "]["
							+ timer.getName() + "]");
				}
				timer.resetExecCount();
				lastRun = now;
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
		if (!isRunning())
			return res;
		if (LOG.isTraceEnabled())
			LOG.trace("add task [" + t + "]");
		t.setQueue(this);

		res = queue.add(t);
		if (queue.peek() == t)
			isFilled.call();
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
	public ScheduledTaskAdapter add(final int intervall, final Runnable run) {
		final ScheduledTaskAdapter cap = new ScheduledTaskAdapter(intervall, 0, run);
		return add(cap) ? cap : null;
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
	 * @return queue size
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * @return true when timer thread is looping
	 */
	public boolean isRunning() {
		return timer.isRunning() || timer.isAlive();
	}

	/**
	 * stops proccessing
	 */
	public void shutdown() {
		if (diagnostic != null)
			diagnostic.stop();
		final ScheduledTask poison = new ScheduledTask(0, 100) {
			@Override
			public void innerRun() {
				if (size() > 0) {
					setNext(100);
					add(this);
				} else
					timer.shutdown();
			}
		};
		add(poison);
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
		public static AtomicInteger COUNTER = new AtomicInteger();
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
		 * observes tq lifecycle
		 */
		private IQueueObserver qobs;

		/**
		 * to get post initialized by extern queue
		 *
		 * @param name
		 */
		public QueueTimer(final String name) {
			super(name + " " + TimeoutQueue.class.getSimpleName() + "-" + COUNTER.incrementAndGet(), false);
		}

		public void setQueueObserver(final IQueueObserver qobs) {
			this.qobs = qobs;
		}

		public void resetExecCount() {
			execCount = 0;
		}

		public int getExecCount() {
			return execCount;
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
			final PriorityBlockingQueue<ITask> q = tq.queue;
			ITask nt = null;
			while (isRunning()) {
				final int state = tq.isFilled.getState();
				if ((nt = q.poll()) == null) {
					tq.isFilled.await(state);
					continue;
				}

				final long nextRun = nt.nextRun();
				long wns = 0;
				if (nextRun > 0 && (wns = nextRun - System.nanoTime()) > 0l) {
					q.add(nt);
					tq.isFilled.await(state, wns);
					continue;
				}

				// enable on demand
				// if (wns < -3000000)
				// LOG.error("Task is out of planning [" + wns / 1000000 + "]["
				// + nt + "]");
				timedOut(nt, wns);
			}
			LOG.info("Exit TimeOutQueue Timer Thread [" + getName() + "]");
			if (qobs != null)
				qobs.queueExited();

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
			tq.run(nt, wns);
		}

		/**
		 * dont proccess anymore
		 */
		public void shutdown() {
			synchronized (tq.queue) {
				running = false;
				tq.isFilled.call();
			}
		}

		/**
		 * @return while loop is active
		 */
		public boolean isRunning() {
			return running;
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
			if (t1 == t2)
				throw new IllegalStateException("Same object allready in queue [" + t1 + "][" + t2 + "]");
			return Primitive.compare(t1.nextRunFixed(), t2.nextRunFixed());
		}
	}

	/**
	 * observer interface for timeout queue
	 *
	 * @author Xyan
	 *
	 */
	public static interface IQueueTaskObserver {

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

	/**
	 * observer interface for timeoutqueue lifecycle
	 *
	 * @author Xyan
	 *
	 */
	public static interface IQueueObserver {
		public void queueExited();
	}
}
