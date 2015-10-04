package net.xy.codebase.cfg;

import java.util.Map;

import net.xy.codebase.cfg.TypeParser.ITypeConverter;

/**
 * configuration object build for different config sources. uses an type parser
 * for pre converting the values.
 *
 * @author Xyan
 *
 */
public class AbstractConfig<Key, Value> {
	/**
	 * value store map
	 */
	protected final Map<Key, Value> values;
	/**
	 * an possible parent config to fallback on
	 */
	private AbstractConfig<Key, Value> parent;
	/**
	 * this typeparser instance
	 */
	protected final TypeParser parser = new TypeParser();

	/**
	 * adds an custom type parser
	 *
	 * @param name
	 * @param parser
	 */
	public void addType(final String name, final ITypeConverter<?> parser) {
		this.parser.add(name, parser);
	}

	/**
	 * default
	 *
	 * @param values
	 *            custom store
	 */
	public AbstractConfig(final Map<Key, Value> values) {
		this.values = values;
	}

	/**
	 * gets an precasted value or the parents value or the default
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(final Key key, final T def) {
		T res = null;
		try {
			res = (T) values.get(key);
			if (def != null && res != null && !def.getClass().isInstance(res))
				throw new RuntimeException(
						"Error wrong type requested [" + def.getClass() + "][" + res.getClass() + "]");
		} catch (final ClassCastException e) {
			throw new RuntimeException(e);
		}
		if (res == null && parent != null)
			res = parent.getValue(key, def);
		if (res != null)
			return res;
		return def;
	}

	/**
	 * uses the type parser to put an value
	 *
	 * @param key
	 * @param val
	 */
	public void putObject(final Key key, final String val) {
		@SuppressWarnings("unchecked")
		final Value value = (Value) parser.string2type(val.trim());
		values.put(key, value);
	}

	/**
	 * puts an value direct without parsing
	 *
	 * @param key
	 * @param val
	 */
	public void putValue(final Key key, final Value val) {
		values.put(key, val);
	}

	/**
	 * sets an parent and fallback config
	 *
	 * @param parent
	 */
	public void setParent(final AbstractConfig<Key, Value> parent) {
		this.parent = parent;
	}

	/**
	 * gets the parent and fallback config
	 *
	 * @return
	 */
	public AbstractConfig<Key, Value> getParent() {
		return parent;
	}
}
