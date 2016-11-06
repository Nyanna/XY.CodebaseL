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
	private EnumMap<E, TrackingQueue> ctxs;
	/**
	 * timeout queue for delayed interthread execution
	 */
	private final TimeoutQueue tque;
	/**
	 * for diagnostics output
	 */
	private StringBuilder sb;

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
		ctxs = new EnumMap<E, TrackingQueue>(enun);
		for (final E val : evals)
			ctxs.put(val, new TrackingQueue(new ParkingQueue<Runnable>(Runnable.class, capacity)));
		addDiagnosticTask();
	}

	/**
	 * with custom queue mappings
	 *
	 * @param giv
	 */
	public InterThreads(final Class<E> enun, final EnumMap<E, ParkingQueue<Runnable>> giv) {
		this();
		ctxs = new EnumMap<E, TrackingQueue>(enun);
		for (final Entry<E, ParkingQueue<Runnable>> val : giv.entrySet())
			ctxs.put(val.getKey(), new TrackingQueue(val.getValue()));
		addDiagnosticTask();
	}

	private void addDiagnosticTask() {
		if (LOG.isDebugEnabled())
			LOG.debug("Created task broker [" + ctxs.size() + "][" + this + "]");
		tque.add(5 * 1000, new Runnable() {
			@Override
			public void run() {
				printDiagnostics();
			}
		});
	}

	protected void printDiagnostics() {
		if (!LOG.isDebugEnabled())
			return;
		if (sb == null)
			sb = new StringBuilder();
		sb.setLength(0);

		sb.append("Tasks ques:\n");
		for (final Entry<E, TrackingQueue> entry : ctxs.entrySet()) {
			final TrackingQueue que = entry.getValue();
			sb.append(String.format("%-12s", entry.getKey())).append("|");
			sb.append(String.format("%3s", que.size())).append(" q|");
			sb.append("+").append(String.format("%6s", que.added.get())).append(" ad|");
			sb.append(String.format("%7s", que.removed.get())).append(" re|");
			sb.append("\n");
			que.reset();
		}

		sb.setLength(sb.length() - 1);
		LOG.info(sb.toString());
	}

	/**
	 * @param target
	 * @return the tragte thread queue
	 */
	protected TrackingQueue get(final E target) {
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
		final TrackingQueue que = get(target);
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
