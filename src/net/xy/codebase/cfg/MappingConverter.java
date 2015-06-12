package net.xy.codebase.cfg;

import java.util.Map.Entry;

import net.xy.codebase.cfg.TypeParser.ITypeConverter;
import net.xy.codebase.collection.Array;

public class MappingConverter<KT, VT> implements ITypeConverter<Array<Entry<KT, VT>>> {

	private final TypeParser parser;

	public MappingConverter(final TypeParser parser) {
		this.parser = parser;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Array<Entry<KT, VT>> parse(final String str) {
		final Array<Entry<KT, VT>> res = new Array<>(Entry.class);

		final String[] pairs = str.split(",");
		for (final String pair : pairs) {
			final String[] kv = pair.split("=", 2);
			res.add(new MappingEntry<KT, VT>((KT) parser.string2type(kv[0]), (VT) parser.string2type(kv[1])));
		}
		res.shrink();
		return res;
	}

	public static class MappingEntry<K, V> implements Entry<K, V> {
		private K key;
		private V val;

		public MappingEntry(final K key) {
			this(key, null);
		}

		public MappingEntry(final K key, final V val) {
			this.key = key;
			this.val = val;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return val;
		}

		@Override
		public V setValue(final V value) {
			return val;
		}

	}
}
