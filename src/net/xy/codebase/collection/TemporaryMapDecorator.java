package net.xy.codebase.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TemporaryMapDecorator<Key, Value> implements Map<Key, Value> {
	private final int timeout;
	private final long creationTime;
	private final Map<Key, TemporaryValue<Value>> delegate;
	private IRemoveObserver<Key, Value> obs;
	private int lastCheck;

	public TemporaryMapDecorator(final int timeout, final Map<Key, TemporaryValue<Value>> delegate) {
		this.timeout = timeout;
		this.creationTime = System.currentTimeMillis();
		this.delegate = delegate;
	}

	public void setRemoveObserver(final IRemoveObserver<Key, Value> obs) {
		this.obs = obs;
	}

	public void checkExpire(final int recheckTimeMs) {
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);
		if (lastCheck + recheckTimeMs > creationOff)
			return;
		lastCheck = creationOff;

		final Iterator<Key> i = delegate.keySet().iterator();
		while (i.hasNext()) {
			final Key key = i.next();
			final TemporaryValue<Value> val = delegate.get(key);

			if (val == null || val.getCreationOff() + timeout < creationOff) {
				i.remove();
				if (obs != null)
					obs.removed(key, val.getValue());
			}
		}
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean containsKey(final Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(final Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Value get(final Object key) {
		return get(key, false);
	}

	@SuppressWarnings("unchecked")
	public Value get(final Object key, final boolean expired) {
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);

		final TemporaryValue<Value> res = delegate.get(key);
		if (res == null)
			return null;
		if (!expired && res.getCreationOff() + timeout < creationOff) {
			delegate.remove(key);
			if (obs != null)
				obs.removed((Key) key, res.getValue());
			return null;
		}

		res.setCreationOff(creationOff);
		return res.getValue();
	}

	@Override
	public Value put(final Key key, final Value value) {
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);

		TemporaryValue<Value> tval = delegate.get(key);
		Value old = null;
		if (tval == null) {
			tval = new TemporaryValue<Value>(value, creationOff);
			delegate.put(key, tval);
		} else {
			old = tval.getValue();
			tval.setValue(value);
			tval.setCreationOff(creationOff);
		}
		return old;
	}

	public void touch(final Key key) {
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);

		final TemporaryValue<Value> tval = delegate.get(key);
		if (tval != null)
			tval.setCreationOff(creationOff);
	}

	@Override
	public Value remove(final Object key) {
		final TemporaryValue<Value> old = delegate.remove(key);
		return old != null ? old.getValue() : null;
	}

	@Override
	public void putAll(final Map<? extends Key, ? extends Value> m) {
		for (final Entry<? extends Key, ? extends Value> entry : m.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Set<Key> keySet() {
		checkExpire(timeout);
		return delegate.keySet();
	}

	@Override
	public Collection<Value> values() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<Key, Value>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public static class TemporaryValue<Value> {
		private Value val;
		private int creationOff;

		public TemporaryValue(final Value val, final int creationOff) {
			this.val = val;
			this.creationOff = creationOff;
		}

		public Value getValue() {
			return val;
		}

		public int getCreationOff() {
			return creationOff;
		}

		public void setCreationOff(final int creationOff) {
			this.creationOff = creationOff;
		}

		public Value setValue(final Value value) {
			final Value old = val;
			val = value;
			return old;
		}
	}

	public static interface IRemoveObserver<Key, Value> {
		public void removed(Key key, Value val);
	}
}
