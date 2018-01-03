package net.xy.codebase.exec;

import java.util.EnumMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ParkingQueue;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.InterThreadIntervall;
import net.xy.codebase.exec.tasks.InterThreadRunnable;
import net.xy.codebase.exec.tasks.InterThreadSchedulable;
import net.xy.codebase.exec.tasks.InterThreadTimeoutable;
import net.xy.codebase.exec.tasks.PriorityInterThreadRunnable;
import net.xy.codebase.exec.tasks.RecurringTask;

/**
 * implementation for inter thread job execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum of possible thread categories
 */
public class InterThreads<E extends Enum<E>> extends AbstractInterThreads<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreads.class);
	/**
	 * thread job category stores
	 */
	protected EnumMap<E, TrackingQueue<Runnable>> ctxs;
	/**
	 * timeout queue for delayed task execution
	 */
	protected final TimeoutQueue tque;

	/**
	 * inner, initializing common fields
	 */
	private InterThreads() {
		tque = new TimeoutQueue("InterThreads");
	}

	/**
	 * default
	 *
	 * @param enun
	 * @param maxCapacity
	 */
	public InterThreads(final Class<E> enun, final int maxCapacity) {
		this();
		final E[] evals = enun.getEnumConstants();
		ctxs = new EnumMap<E, TrackingQueue<Runnable>>(enun);
		for (final E val : evals)
			ctxs.put(val, new TrackingQueue<Runnable>(new ParkingQueue<Runnable>(Runnable.class, maxCapacity)));
	}

	/**
	 * with custom queue mappings
	 *
	 * @param giv
	 */
	public InterThreads(final Class<E> enun, final EnumMap<E, ParkingQueue<Runnable>> giv) {
		this();
		ctxs = new EnumMap<E, TrackingQueue<Runnable>>(enun);
		for (final Entry<E, ParkingQueue<Runnable>> val : giv.entrySet())
			ctxs.put(val.getKey(), new TrackingQueue<Runnable>(val.getValue()));
	}

	@Override
	public void setObserver(final net.xy.codebase.exec.IInterThreads.IJobObserver<E> obs) {
		super.setObserver(obs);
		tque.setObserver(obs);
	}

	/**
	 * @param target
	 * @return the tragte thread queue
	 */
	protected TrackingQueue<Runnable> get(final E target) {
		return ctxs.get(target);
	}

	public int getQueueAmount() {
		return ctxs.size();
	}

	@Override
	public Runnable next(final E target, final int ms) {
		final TrackingQueue<Runnable> que = get(target);
		if (que == null)
			throw new IllegalArgumentException("Target job queue don't exists [" + target + "]");
		return que.take(ms);
	}

	@Override
	public boolean run(final E target, final Runnable job) {
		final TrackingQueue<Runnable> que = get(target);
		if (que == null) {
			LOG.error("Target job queue don't exists [" + target + "][" + job + "]");
			return false;
		}
		if (!que.add(job)) {
			if (obs != null)
				obs.jobDroped(target, job, que.size());
			else
				LOG.error("Target thread too full droping job [" + target + "][" + que.size() + "][" + job + "]");
			return false;
		} else if (obs != null)
			obs.jobAdded(target, job);
		return true;
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run) {
		return new ExecutionThrottler(new InterThreadRunnable<E>(thread, run, this));
	}

	@Override
	public ExecutionThrottler getPriorityThrottler(final E thread, final Runnable run, final int priority) {
		return new ExecutionThrottler(new PriorityInterThreadRunnable<E>(thread, run, this, priority));
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run, final int intervallMs) {
		return new ExecutionThrottler(new InterThreadSchedulable<E>(thread, run, this), intervallMs);
	}

	@Override
	public ExecutionLimiter getLimiter(final E thread, final Runnable run, final int amount) {
		return new ExecutionLimiter(new InterThreadRunnable<E>(thread, run, this), amount);
	}

	@Override
	public ExecutionLimiter getPriorityLimiter(final E thread, final Runnable run, final int priority,
			final int amount) {
		return new ExecutionLimiter(new PriorityInterThreadRunnable<E>(thread, run, this, priority), amount);
	}

	@Override
	public InterThreadTimeoutable<E> runLater(final E thread, final Runnable run, final int timeout) {
		final InterThreadTimeoutable<E> res = new InterThreadTimeoutable<E>(thread, timeout, run, this);
		return start(res) ? res : null;
	}

	@Override
	public RecurringTask startIntervall(final E thread, final Runnable run, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, 0, run, this);
		return start(res) ? res : null;
	}

	@Override
	public RecurringTask startDelayed(final E thread, final Runnable run, final int startDelay, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, startDelay, run, this);
		return start(res) ? res : null;
	}

	@Override
	public boolean start(final ITask task) {
		return tque.add(task);
	}

	public void shutdown() {
		tque.shutdown();
	}
}
