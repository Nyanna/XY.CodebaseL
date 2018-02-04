package net.xy.codebase.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.xy.codebase.exec.tasks.ICoveredRunnable;

public class TrackingJobObserver<J> extends JobObserver<J> {
	private final Map<J, Map<Class<?>, ExecutionTracker>> exec = new HashMap<J, Map<Class<?>, ExecutionTracker>>();

	private void trackEnd(final J target, final Runnable res, final long tok) {
		final Runnable real = getRealRunnable(res);

		final ExecutionTracker trk = getOrCreateTracker(target, real);
		trk.executed(tok);
	}

	private void trackAdd(final J target, final Runnable res) {
		final Runnable real = getRealRunnable(res);

		final ExecutionTracker trk = getOrCreateTracker(target, real);
		trk.added();
	}

	@Override
	public void jobAdded(final J target, final Runnable job) {
		super.jobAdded(target, job);
		trackAdd(target, job);
	}

	@Override
	public void jobEnd(final J target, final Runnable job, final IPerfCounter measure, final long duration) {
		super.jobEnd(target, job, measure, duration);
		trackEnd(target, job, duration);
	}

	private Runnable getRealRunnable(final Runnable res) {
		Runnable real = res;
		while (real instanceof ICoveredRunnable) {
			final Runnable next = ((ICoveredRunnable) real).getRunnable();
			if (next == null)
				break;
			real = next;
		}
		return real;
	}

	private ExecutionTracker getOrCreateTracker(final J target, final Runnable real) {
		Map<Class<?>, ExecutionTracker> store = exec.get(target);
		if (store == null)
			synchronized (exec) {
				store = exec.get(target);
				if (store == null)
					exec.put(target, store = new ConcurrentHashMap<Class<?>, TrackingJobObserver.ExecutionTracker>());

			}

		final Class<?> cc = real.getClass();
		ExecutionTracker trk = store.get(cc);
		if (trk == null)
			synchronized (store) {
				trk = store.get(cc);
				if (trk == null) {
					store.put(cc, trk = new ExecutionTracker());
					trackerCreated(target, cc, trk);
				}
			}
		return trk;
	}

	public Map<J, Map<Class<?>, ExecutionTracker>> getStats() {
		return exec;
	}

	protected void trackerCreated(final J target, final Class<?> cc, final ExecutionTracker trk) {
		// to be overidden
	}

	public static class ExecutionTracker {
		public int execAmn;
		public long execTime;
		public long lastExec;
		public long added;

		public void executed(final long tok) {
			execAmn++;
			execTime += tok;
			lastExec = System.currentTimeMillis();
		}

		public void added() {
			added++;
		}

		public float avrNs() {
			return (float) execTime / execAmn;
		}
	}
}
