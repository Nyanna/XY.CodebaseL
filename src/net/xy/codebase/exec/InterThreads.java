package net.xy.codebase.exec;

import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xy.codebase.collection.ParkingArrayQueue;
import net.xy.codebase.exec.tasks.ITask;
import net.xy.codebase.exec.tasks.InterThreadIntervall;
import net.xy.codebase.exec.tasks.InterThreadRunnable;
import net.xy.codebase.exec.tasks.InterThreadSchedulable;
import net.xy.codebase.exec.tasks.InterThreadTimeoutable;
import net.xy.codebase.exec.tasks.RecurringTask;

/**
 * implementation for inter thread job execution
 *
 * @author Xyan
 *
 * @param <E>
 *            enum of possible threads
 */
public class InterThreads<E extends Enum<E>> extends AbstractInterThreads<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreads.class);
	/**
	 * thread job stores
	 */
	private final EnumMap<E, ParkingArrayQueue<Runnable>> ctxs;
	/**
	 * timeout queue for delayed interthread execution
	 */
	private final TimeoutQueue tque;

	/**
	 * default
	 *
	 * @param enun
	 * @param capacity
	 */
	public InterThreads(final Class<E> enun, final int capacity) {
		final E[] evals = enun.getEnumConstants();
		ctxs = new EnumMap<E, ParkingArrayQueue<Runnable>>(enun);
		for (final E val : evals)
			ctxs.put(val, new ParkingArrayQueue<Runnable>(Runnable.class, capacity));

		tque = new TimeoutQueue("Interthread");
	}

	/**
	 * @param target
	 * @return the tragte thread queue
	 */
	private ParkingArrayQueue<Runnable> get(final E target) {
		return ctxs.get(target);
	}

	@Override
	public Runnable next(final E target) {
		return get(target).take();
	}

	@Override
	public Runnable next(final E target, final int ms) {
		return get(target).take(ms);
	}

	@Override
	public void put(final E target, final Runnable job) {
		final ParkingArrayQueue<Runnable> que = get(target);
		if (!que.add(job))
			LOG.error("Error target thread too full droping job [" + target + "][" + job + "]");
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run) {
		return new ExecutionThrottler(new InterThreadRunnable<E>(thread, run, this));
	}

	@Override
	public ExecutionThrottler getThrottler(final E thread, final Runnable run, final int intervallMs) {
		return new ExecutionThrottler(new InterThreadSchedulable<E>(thread, run, this), intervallMs);
	}

	@Override
	public InterThreadTimeoutable<E> runLater(final E thread, final Runnable run, final int timeout) {
		final InterThreadTimeoutable<E> res = new InterThreadTimeoutable<E>(thread, timeout, run, this);
		tque.add(res);
		return res;
	}

	@Override
	public RecurringTask start(final E thread, final Runnable run, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, run, this);
		tque.add(res);
		return res;
	}

	@Override
	public RecurringTask start(final E thread, final Runnable run, final int startIn, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, startIn, run, this);
		tque.add(res);
		return res;
	}

	@Override
	public void start(final ITask task) {
		tque.add(task);
	}
}
