package net.xy.codebase.cfg;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * configuration object build for different config sources. uses an type parser
 * for pre converting the values. enummap specialized variant.
 *
 * @author Xyan
 *
 */
public class EnumConfig<Key extends Enum<Key>> extends AbstractConfig<Key, Object> {
	/**
	 * enum key type
	 */
	private final Class<Key> keyType;

	/**
	 * default with enummap
	 *
	 * @param keyType
	 */
	public EnumConfig(final Class<Key> keyType) {
		super(new EnumMap<Key, Object>(keyType));
		this.keyType = keyType;
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
			values.put(valueOf(key), val);
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
			values.put(valueOf(key), val);
		}
		return true;
	}

	/**
	 * does an enum like valueof, case insensitive
	 *
	 * @param key
	 * @return
	 */
	private Key valueOf(final String key) {
		final Key[] consts = keyType.getEnumConstants();
		for (final Key con : consts)
			if (con.name().equalsIgnoreCase(key))
				return con;
		throw new IllegalArgumentException("No valid enumkey [" + key + "]");
	}
}
