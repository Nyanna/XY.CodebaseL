package net.xy.codebase.exec;

import java.util.EnumMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ParkingQueue;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.InterThreadScheduledTask;
import net.xy.codebase.exec.tasks.ScheduledTask;

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
	protected final InterTimeoutQueue tque;

	/**
	 * inner, initializing common fields
	 */
	private InterThreads() {
		tque = new InterTimeoutQueue("InterThreads", this);
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
		tque.addObserver(obs);
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
		final Runnable runnable = que.take(ms);
		if (LOG.isTraceEnabled() && runnable != null)
			LOG.trace("Took job from que [" + target + "][" + runnable + "]");
		return runnable;
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
		} else if (obs != null) {
			if (LOG.isTraceEnabled())
				LOG.trace("Adding job to que [" + target + "][" + que.size() + "][" + job + "]");
			obs.jobAdded(target, job);
		}
		return true;
	}

	@Override
	public ExecutionThrottler throttled(final E thread, final Runnable run) {
		return new ExecutionThrottler(run, this, thread);
	}

	@Override
	public ExecutionThrottler throttled(final E thread, final Runnable run, final int intervallMs) {
		return new ExecutionThrottler(run, intervallMs, this, thread);
	}

	@Override
	public ExecutionLimiter limited(final E thread, final Runnable run, final int amount) {
		return new ExecutionLimiter(run, amount, this, thread);
	}

	@Override
	public InterThreadScheduledTask<E> runLater(final E thread, final Runnable run, final int timeout) {
		return tque.runLater(thread, run, timeout);
	}

	@Override
	public ScheduledTask runIntervall(final E thread, final Runnable run, final int intervall) {
		return tque.runIntervall(thread, run, intervall);
	}

	@Override
	public ScheduledTask runDelayedIntervall(final E thread, final Runnable run, final int intervall,
			final int startDelay) {
		return tque.runDelayedIntervall(thread, run, intervall, startDelay);
	}

	@Override
	public boolean start(final ITask task) {
		return tque.add(task);
	}

	public void shutdown() {
		tque.shutdown();
	}
}
