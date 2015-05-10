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
public class Config extends AbstractConfig<String, Object> {

	/**
	 * default based on HashMap
	 */
	public Config() {
		super(new HashMap<String, Object>());
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
}
