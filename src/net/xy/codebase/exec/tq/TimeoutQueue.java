package net.xy.codebase.exec.tq;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.asserts.Assert;
import net.xy.codebase.concurrent.Semaphore;
import net.xy.codebase.exec.tasks.ITask;

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
	 * counter for queue numbering
	 */
	public static AtomicInteger COUNTER = new AtomicInteger();
	/**
	 * ordered task queue
	 */
	private final PriorityBlockingQueue<ITask> queue;
	/**
	 * timer thread
	 */
	private final QueueTimerThread timer;
	/**
	 * observe queue task processing
	 */
	private final QueueObserverMulticaster obs = new QueueObserverMulticaster();
	/**
	 * diagnostics timer
	 */
	private DiagnosticTask diagnostic;
	/**
	 * for waits in empty ques
	 */
	private final Semaphore added = new Semaphore();
	/**
	 * name to identify this queue
	 */
	private final String name;

	/**
	 * default
	 */
	public TimeoutQueue(final String name) {
		this(new QueueTimerThread(name), name);
	}

	/**
	 * for external provided thread
	 *
	 * @param thread
	 */
	public TimeoutQueue(final QueueTimerThread thread, final String name) {
		this.name = name + " " + TimeoutQueue.class.getSimpleName() + "-" + COUNTER.incrementAndGet();
		queue = new PriorityBlockingQueue<ITask>(100, new TaskComparator());
		thread.setQueue(queue);
		thread.setCondition(added);
		thread.setObserver(obs);
		thread.setPriority(Thread.MAX_PRIORITY);
		if (!thread.isAlive())
			thread.start();
		timer = thread;
		if (LOG.isDebugEnabled())
			LOG.debug("Created TimeOutQueue [" + thread + "][" + thread.getName() + "]");
		add(diagnostic = new DiagnosticTask(30 * 1000, this));
		obs.addObserver(diagnostic);
	}

	/**
	 * unique name of this queue
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * adds a observer to the multicaster stack
	 *
	 * @param obs
	 */
	public void addObserver(final IQueueObserver obs) {
		this.obs.addObserver(obs);
	}

	/**
	 * adds an task for scheduling
	 *
	 * @param t
	 */
	public boolean add(final ITask t) {
		if (!isRunning())
			return false;

		Assert.True(t != null, "Task should not be null");

		if (LOG.isTraceEnabled())
			LOG.trace("add task [" + t + "]");
		t.setQueue(this);

		final boolean res = queue.add(t);
		if (queue.peek() == t)
			added.call();
		if (!res)
			LOG.error("Error inserting task into timeout que [" + t + "][" + timer.getName() + "]");
		else if (obs != null)
			obs.taskAdded(t);
		return res;
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
		return timer != null && (timer.isRunning() || timer.isAlive());
	}

	/**
	 * stops proccessing
	 */
	public void shutdown() {
		if (diagnostic != null)
			diagnostic.stop();
		if (timer != null)
			timer.shutdown();
	}
}
