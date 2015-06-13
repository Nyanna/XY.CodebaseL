package net.xy.codebase.mem;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;

public class MonitorReference<T> extends PhantomReference<T> {

	private final ReferenceQueue<?> que;
	private Field fnext;
	private Field fdisc;
	private Field fque;
	private Field fref;

	public MonitorReference(final T obj, final ReferenceQueue<? super T> que) {
		super(obj, que);

		this.que = que;
		try {
			fnext = Reference.class.getDeclaredField("next");
			fnext.setAccessible(true);
			fdisc = Reference.class.getDeclaredField("discovered");
			fdisc.setAccessible(true);
			fque = Reference.class.getDeclaredField("queue");
			fque.setAccessible(true);
			fref = Reference.class.getDeclaredField("referent");
			fref.setAccessible(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public T get() {
		try {
			reset();
			return getReferent();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void reset() throws IllegalArgumentException, IllegalAccessException {
		fnext.set(this, null);
		fdisc.set(this, null);
		fque.set(this, que);
	}

	@SuppressWarnings("unchecked")
	private T getReferent() throws IllegalArgumentException, IllegalAccessException {
		return (T) fref.get(this);
	}
}
