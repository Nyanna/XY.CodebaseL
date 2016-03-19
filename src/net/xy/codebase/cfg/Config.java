package net.xy.codebase.cfg;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * configuration object build for different config sources. uses an type parser
 * for pre converting the values.
 *
 * @author Xyan
 *
 */
public class Config<Key, Value> extends AbstractConfig<Key, Value> {

	/**
	 * default based on HashMap
	 */
	public Config() {
		super(new HashMap<Key, Value>());
	}

	/**
	 * default with given map
	 */
	public Config(final HashMap<Key, Value> cfg) {
		super(cfg);
	}

	/**
	 * default with given map & parent
	 */
	public Config(final HashMap<Key, Value> cfg, final AbstractConfig<Key, Value> parent) {
		super(cfg);
		setParent(parent);
	}

	/**
	 * default with parent
	 */
	public Config(final AbstractConfig<Key, Value> parent) {
		super(new HashMap<Key, Value>());
		setParent(parent);
	}

	/**
	 * fills the config from cli main args, by key=val pattern
	 *
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean parse(final String[] args) {
		for (final String arg : args) {
			final String[] pair = arg.split("=", 2);
			final String key = pair[0].trim();
			final Object val = parser.string2type(pair[1].trim());
			values.put((Key) key, (Value) val);
		}
		return true;
	}

	/**
	 * parses an already filled property file for types
	 *
	 * @param props
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean parse(final Properties props) {
		for (final Entry<Object, Object> entry : props.entrySet()) {
			final String key = ((String) entry.getKey()).trim();
			final Object val = parser.string2type(((String) entry.getValue()).trim());
			values.put((Key) key, (Value) val);
		}
		return true;
	}
}
