package net.xy.codebase.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import net.xy.codebase.cfg.Typeparser.ITypeConverter;

/**
 * configuration object build for different config sources. uses an type parser
 * for pre converting the values.
 *
 * @author Xyan
 *
 */
public class Config {
	/**
	 * value store map
	 */
	private final Map<String, Object> values = new HashMap<String, Object>();
	/**
	 * an possible parent config to fallback on
	 */
	private Config parent;
	/**
	 * this typeparser instance
	 */
	private final Typeparser parser = new Typeparser();

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
	 * fills the config from cli main args, by key=val pattern
	 *
	 * @param args
	 * @return
	 */
	public boolean parse(final String[] args) {
		for (final String arg : args) {
			final String[] pair = arg.split("=", 2);
			final String key = pair[0].trim();
			final Object val = parser.string2type(pair[1].trim());
			values.put(key, val);
		}
		return true;
	}

	/**
	 * parses an already filled property file for types
	 *
	 * @param props
	 * @return
	 */
	public boolean parse(final Properties props) {
		for (final Entry<Object, Object> entry : props.entrySet()) {
			final String key = ((String) entry.getKey()).trim();
			final Object val = parser.string2type(((String) entry.getValue()).trim());
			values.put(key, val);
		}
		return true;
	}

	/**
	 * gets an precasted value or the parents value or the default
	 *
	 * @param key
	 * @param def
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValue(final String key, final T def) {
		T res = null;
		try {
			res = (T) values.get(key);
		} catch (final ClassCastException ex) {
			ex.printStackTrace();
		}
		if (res == null && parent != null)
			res = parent.getValue(key, def);
		if (res != null)
			return res;
		return def;
	}

	/**
	 * sets an parent and fallback config
	 *
	 * @param parent
	 */
	public void setParent(final Config parent) {
		this.parent = parent;
	}

	/**
	 * gets the parent and fallback config
	 *
	 * @return
	 */
	public Config getParent() {
		return parent;
	}
}
