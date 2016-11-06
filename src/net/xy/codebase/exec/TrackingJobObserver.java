package net.xy.codebase.exec;

import java.util.HashMap;
import java.util.Map;

import net.xy.codebase.exec.IInterThreads.IJobObserver;
import net.xy.codebase.exec.tasks.ICoveredRunnable;

public class TrackingJobObserver<J> implements IJobObserver<J> {
	private Map<Class<?>, ExecutionTracker> exec = new HashMap<Class<?>, ExecutionTracker>();
	private long start = 0;

	protected void track(final Runnable res, final long tok) {
		Runnable real = res;
		while (real instanceof ICoveredRunnable) {
			final Runnable next = ((ICoveredRunnable) real).getRunnable();
			if (next == null)
				break;
			real = next;
		}

		final Class<?> cc = real.getClass();
		ExecutionTracker trk = exec.get(cc);
		if (trk == null)
			exec.put(cc, trk = new ExecutionTracker());
		trk.executed(tok);
	}

	public static class ExecutionTracker {
		public int execAmn;
		public long execTime;

		public void executed(final long tok) {
			execAmn++;
			execTime += tok;
		}

		public float avrMs() {
			return (float) execTime / execAmn;
		}
	}

	@Override
	public boolean startJob(final J target, final Runnable job) {
		start = System.nanoTime();
		return true;
	}

	@Override
	public void endJob(final J target, final Runnable job) {
		track(job, System.nanoTime() - start);
	}

	public Map<Class<?>, ExecutionTracker> getAndReset() {
		final Map<Class<?>, ExecutionTracker> res = exec;
		exec = new HashMap<Class<?>, ExecutionTracker>();
		return res;
	}
}
