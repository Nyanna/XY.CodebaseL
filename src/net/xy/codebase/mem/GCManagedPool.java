package net.xy.codebase.mem;

import java.lang.ref.ReferenceQueue;

import net.xy.codebase.collection.Array;

public abstract class GCManagedPool<T> extends ConcurrentPool<T> {
	private final ReferenceQueue<T> que = new ReferenceQueue<>();
	private final Array<MonitorReference<T>> wrs = new Array<>(MonitorReference.class);

	@SuppressWarnings("unchecked")
	public void checkQue() {
		MonitorReference<T> ref;
		while ((ref = (MonitorReference<T>) que.poll()) != null)
			free(ref.get());
	}

	@Override
	public T obtain() {
		checkQue();
		return super.obtain();
	}

	@Override
	protected T newObject() {
		final T res = createObject();
		wrs.addChecked(new MonitorReference<T>(res, que));
		return res;
	}

	protected abstract T createObject();
}
