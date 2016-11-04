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
 *            enum of possible threads
 */
public class InterThreads<E extends Enum<E>> extends AbstractInterThreads<E> {
	private static final Logger LOG = LoggerFactory.getLogger(InterThreads.class);
	/**
	 * thread job stores
	 */
	private EnumMap<E, ParkingQueue<Runnable>> ctxs;
	/**
	 * timeout queue for delayed interthread execution
	 */
	private final TimeoutQueue tque;

	/**
	 * inner, initializing common fields
	 */
	private InterThreads() {
		tque = new TimeoutQueue("Interthread");
	}

	/**
	 * default
	 *
	 * @param enun
	 * @param capacity
	 */
	public InterThreads(final Class<E> enun, final int capacity) {
		this();
		final E[] evals = enun.getEnumConstants();
		ctxs = new EnumMap<E, ParkingQueue<Runnable>>(enun);
		for (final E val : evals)
			ctxs.put(val, new ParkingQueue<Runnable>(Runnable.class, capacity));
		addDiagnosticTask();
	}

	/**
	 * with custom queue mappings
	 *
	 * @param ctxs
	 */
	public InterThreads(final EnumMap<E, ParkingQueue<Runnable>> ctxs) {
		this();
		this.ctxs = ctxs;
		if (LOG.isDebugEnabled())
			LOG.debug("Created task broker [" + ctxs.size() + "][" + this + "]");
		addDiagnosticTask();
	}

	private void addDiagnosticTask() {
		tque.add(30 * 1000, new Runnable() {
			private final StringBuilder sb = new StringBuilder();

			@Override
			public void run() {
				if (LOG.isDebugEnabled()) {
					sb.setLength(0);

					sb.append("Task broker sizes: ");
					for (final Entry<E, ParkingQueue<Runnable>> entry : ctxs.entrySet())
						sb.append("[").append(entry.getKey()).append("=").append(entry.getValue().size()).append("]");

					LOG.debug(sb.toString());
				}
			}
		});
	}

	/**
	 * @param target
	 * @return the tragte thread queue
	 */
	private ParkingQueue<Runnable> get(final E target) {
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
	public boolean put(final E target, final Runnable job) {
		final ParkingQueue<Runnable> que = get(target);
		if (!que.add(job)) {
			LOG.error("Error target thread too full droping job [" + target + "][" + que.size() + "][" + job + "]");
			return false;
		}
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
	public ExecutionLimiter getPriorityLimiter(final E thread, final Runnable run, final int priority,
			final int amount) {
		return new ExecutionLimiter(new PriorityInterThreadRunnable<E>(thread, run, this, priority), amount);
	}

	@Override
	public InterThreadTimeoutable<E> runLater(final E thread, final Runnable run, final int timeout) {
		final InterThreadTimeoutable<E> res = new InterThreadTimeoutable<E>(thread, timeout, run, this);
		return tque.add(res) ? res : null;
	}

	@Override
	public RecurringTask start(final E thread, final Runnable run, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, run, this);
		return tque.add(res) ? res : null;
	}

	@Override
	public RecurringTask start(final E thread, final Runnable run, final int startIn, final int intervall) {
		final InterThreadIntervall<E> res = new InterThreadIntervall<E>(thread, intervall, startIn, run, this);
		return tque.add(res) ? res : null;
	}

	@Override
	public boolean start(final ITask task) {
		return tque.add(task);
	}
}
