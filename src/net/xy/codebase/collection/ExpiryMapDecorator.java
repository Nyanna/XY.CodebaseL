package net.xy.codebase.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.xy.codebase.collection.TemporaryMapDecorator.TemporaryValue;

public class ExpiryMapDecorator<Key, Value> implements Map<Key, Value> {
	private final int timeout;
	private final long creationTime;
	private final Map<Key, TemporaryValue<Value>> delegate;
	private IExpiryObserver<Key, Value> obs;
	private int lastCheck;

	public ExpiryMapDecorator(final int timeout, final Map<Key, TemporaryValue<Value>> delegate) {
		this.timeout = timeout;
		this.creationTime = System.currentTimeMillis();
		this.delegate = delegate;
	}

	public void setExpiryObserver(final IExpiryObserver<Key, Value> obs) {
		this.obs = obs;
	}

	public void checkExpire() {
		checkExpire(timeout);
	}

	public void checkExpire(final int recheck) {
		checkExpire(recheck, false);
	}

	public void checkExpire(final int recheck, final boolean force) {
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);
		if (!force && lastCheck + recheck > creationOff)
			return;
		lastCheck = creationOff;

		final Iterator<Key> i = delegate.keySet().iterator();
		while (i.hasNext()) {
			final Key key = i.next();
			final TemporaryValue<Value> val = delegate.get(key);

			if (val == null || val.getCreationOff() + timeout < creationOff) {
				i.remove();
				if (obs != null)
					obs.expired(key, val.getValue());
			}
		}
	}

	@Override
	public int size() {
		checkExpire(timeout);
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
		final int creationOff = (int) (System.currentTimeMillis() - creationTime);

		final TemporaryValue<Value> res = delegate.get(key);
		if (res == null)
			return null;

		res.setCreationOff(creationOff);
		return res.getValue();
	}

	@Override
	public Value put(final Key key, final Value value) {
		return put(key, value, System.currentTimeMillis());
	}

	public Value put(final Key key, final Value value, final long now) {
		final int creationOff = (int) (now - creationTime);

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

	public static interface IExpiryObserver<Key, Value> {
		public void expired(Key key, Value val);
	}
}
