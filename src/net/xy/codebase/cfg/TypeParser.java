/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 *
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebase.cfg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utility mainly for string to type conversion
 *
 * @author Xyan
 *
 */
public class TypeParser {
	private static final int MOD = Pattern.CASE_INSENSITIVE;
	private static final String ARRAY_DELIMITER = ",";
	/**
	 * if true disable implecite relaxed checking
	 */
	public boolean STRICT = false;
	// stricts
	private final Pattern PT_STRICT_STRING = Pattern.compile("(.*):String", MOD);
	private final Pattern PT_STRICT_BYTE = Pattern.compile("([0-9\\-]{1,3}):Byte", MOD);
	private final Pattern PT_STRICT_SHORT = Pattern.compile("([0-9\\-]{1,5}):Short", MOD);
	private final Pattern PT_STRICT_INT = Pattern.compile("([0-9\\-]{1,10}):Integer", MOD);
	private final Pattern PT_STRICT_LONG = Pattern.compile("([0-9\\-l]{1,19}):Long", MOD);
	private final Pattern PT_STRICT_FLOAT = Pattern.compile("([0-9,\\-fE]+):Float", MOD);
	private final Pattern PT_STRICT_DOUBLE = Pattern.compile("([0-9.,\\-d]+):Double", MOD);
	private final Pattern PT_STRICT_BOOL = Pattern.compile("(true|false):Boolean", MOD);
	private final Pattern PT_STRICT_CHAR = Pattern.compile("(.{1}):Char", MOD);
	private final Pattern PT_STRING = Pattern.compile("(\".*\")", MOD);
	private final Pattern PT_BYTE = Pattern.compile("x([0-9\\-]{1,3})", MOD);
	private final Pattern PT_SHORT = Pattern.compile("([0-9\\-]{1,5})s", MOD);
	private final Pattern PT_INT = Pattern.compile("([0-9\\-]{1,10})", MOD);
	private final Pattern PT_LONG = Pattern.compile("([0-9\\-l]{1,19})", MOD);
	private final Pattern PT_FLOAT = Pattern.compile("([0-9.,\\-fE]+)", MOD);
	private final Pattern PT_DOUBLE = Pattern.compile("([0-9.,\\-d]+)", MOD);
	private final Pattern PT_BOOL = Pattern.compile("(true|false)", MOD);
	private final Pattern PT_CHAR = Pattern.compile("('.{1}')", MOD);
	// calls an converter like factory method accepting string returning object
	private final Pattern PT_CONVERTER = Pattern.compile("\\[(.*)\\]:([a-zA-Z0-9.$]+)", MOD);
	// creates via reflection an instance with an string constructor
	private final Pattern PT_CUSTOM = Pattern.compile("(.*):([a-zA-Z0-9]+)", MOD);
	// returns as lists
	private final Pattern PT_ARRAY_STRING_SIMPLE = Pattern.compile("\\{(.*)\\}", MOD);
	private final Pattern PT_ARRAY_STRING = Pattern.compile("\\{(.*)\\}:String", MOD);
	private final Pattern PT_ARRAY_BYTE = Pattern.compile("\\{(.*)\\}:Byte", MOD);
	private final Pattern PT_ARRAY_SHORT = Pattern.compile("\\{(.*)\\}:Short", MOD);
	private final Pattern PT_ARRAY_INT = Pattern.compile("\\{(.*)\\}:Integer", MOD);
	private final Pattern PT_ARRAY_LONG = Pattern.compile("\\{(.*)\\}:Long", MOD);
	private final Pattern PT_ARRAY_FLOAT = Pattern.compile("\\{(.*)\\}:Float", MOD);
	private final Pattern PT_ARRAY_DOUBLE = Pattern.compile("\\{(.*)\\}:Double", MOD);
	private final Pattern PT_ARRAY_BOOL = Pattern.compile("\\{(.*)\\}:Boolean", MOD);

	// calls an converter accepting string array returning object list
	// private final Pattern PT_ARRAY_CONVERTER =
	// Pattern.compile("\\{(.*)\\}:([a-zA-Z0-9.$]+)", MOD);
	private final Map<String, ITypeConverter<?>> customConverters = new HashMap<>();

	/**
	 * default, with extended converters
	 */
	public TypeParser() {
		add("Mapping", new MappingConverter<Object, Object>(this));
	}

	/**
	 * adds an custom type parser
	 *
	 * @param name
	 * @param parser
	 */
	public void add(final String name, final ITypeConverter<?> parser) {
		customConverters.put(name, parser);
	}

	/**
	 * delegate without converter and custom type support
	 *
	 * @param string
	 * @param loader
	 * @return
	 */
	public Object string2type(final String string) {
		try {
			return string2type(string, null);
		} catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalArgumentException("Error parsing string to value [" + string + "]", e);
		}
	}

	/**
	 * Converts an string to its java counterpart
	 *
	 * @param loader
	 *            for custom types
	 * @throws ClassNotFoundException
	 *             if custom type could not be found
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public Object string2type(String string, final ClassLoader loader)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (string == null || string.length() == 0)
			return null;
		Matcher match;
		string = string.trim();
		match = PT_ARRAY_STRING.matcher(string);
		if (match.matches())
			return match.group(1).split(ARRAY_DELIMITER);
		match = PT_ARRAY_BYTE.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final byte[] res = new byte[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Byte.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_SHORT.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final short[] res = new short[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Short.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_INT.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final int[] res = new int[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Integer.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_LONG.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final long[] res = new long[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Long.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_FLOAT.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final float[] res = new float[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Float.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_DOUBLE.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final double[] res = new double[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Double.valueOf(vals[i].trim());
			return res;
		}
		match = PT_ARRAY_BOOL.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(ARRAY_DELIMITER);
			final boolean[] res = new boolean[vals.length];
			for (int i = 0; i < res.length; i++)
				res[i] = Boolean.valueOf(vals[i].trim());
			return res;
		}
		match = PT_STRICT_STRING.matcher(string);
		if (match.matches())
			return match.group(1);
		match = PT_STRICT_BYTE.matcher(string);
		if (match.matches())
			return Byte.valueOf(match.group(1));
		match = PT_STRICT_SHORT.matcher(string);
		if (match.matches())
			return Short.valueOf(match.group(1));
		match = PT_STRICT_INT.matcher(string);
		if (match.matches())
			return Integer.valueOf(match.group(1));
		match = PT_STRICT_LONG.matcher(string);
		if (match.matches())
			return Long.valueOf(match.group(1));
		match = PT_STRICT_FLOAT.matcher(string);
		if (match.matches())
			return Float.valueOf(match.group(1).replace(ARRAY_DELIMITER, ".").replace("f", ""));
		match = PT_STRICT_DOUBLE.matcher(string);
		if (match.matches())
			return Double.valueOf(match.group(1).replace(ARRAY_DELIMITER, "."));
		match = PT_STRICT_BOOL.matcher(string);
		if (match.matches())
			return Boolean.valueOf(match.group(1));
		match = PT_STRICT_CHAR.matcher(string);
		if (match.matches())
			return Character.valueOf(match.group(1).charAt(0));
		match = PT_CUSTOM.matcher(string);
		if (match.matches()) {
			final ITypeConverter<?> conv = customConverters.get(match.group(2));
			if (conv != null)
				return conv.parse(match.group(1));
		}
		match = PT_CONVERTER.matcher(string);
		if (match.matches()) {
			final Class<?> clazz = loader.loadClass(match.group(2));
			final Constructor<?> con = clazz.getConstructor(String.class);
			return con.newInstance(match.group(1));
		}
		if (!STRICT) {
			match = PT_STRING.matcher(string);
			if (match.matches())
				return match.group(1);
			match = PT_BYTE.matcher(string);
			if (match.matches())
				return Byte.valueOf(match.group(1));
			match = PT_SHORT.matcher(string);
			if (match.matches())
				return Short.valueOf(match.group(1));
			match = PT_INT.matcher(string);
			if (match.matches())
				return Integer.valueOf(match.group(1));
			match = PT_LONG.matcher(string);
			if (match.matches())
				return Long.valueOf(match.group(1).substring(0, match.group(1).length() - 1));
			match = PT_FLOAT.matcher(string);
			if (match.matches())
				return Float.valueOf(match.group(1).replace(ARRAY_DELIMITER, ".").replace("f", ""));
			match = PT_DOUBLE.matcher(string);
			if (match.matches())
				return Double.valueOf(match.group(1).replace(ARRAY_DELIMITER, "."));
			match = PT_BOOL.matcher(string);
			if (match.matches())
				return Boolean.valueOf(match.group(1));
			match = PT_CHAR.matcher(string);
			if (match.matches())
				return Character.valueOf(match.group(1).charAt(0));
			match = PT_ARRAY_STRING_SIMPLE.matcher(string);
			if (match.matches())
				return Arrays.asList(match.group(1).split(ARRAY_DELIMITER));
		}
		return string;
	}

	/**
	 * converts an type back to an string representation so that can parsed
	 * again, when strict on :Type notation is used
	 *
	 * @param type
	 * @return
	 */
	public String type2String(final Object value) {
		if (value.getClass().isArray()) {
			final StringBuilder res = new StringBuilder("{");
			for (int i = 0; i < ((Object[]) value).length; i++) {
				if (i > 0)
					res.append(";");
				res.append(((Object[]) value)[i]);
			}
			res.append("}");
			final Class<?> clazz = value.getClass().getComponentType();
			if (clazz.isAssignableFrom(String.class))
				res.append(":String");
			else if (clazz.isAssignableFrom(Integer.class))
				res.append(":Integer");
			else if (clazz.isAssignableFrom(Long.class))
				res.append(":Long");
			else if (clazz.isAssignableFrom(Float.class))
				res.append(":Float");
			else if (clazz.isAssignableFrom(Double.class))
				res.append(":Double");
			else if (clazz.isAssignableFrom(Boolean.class))
				res.append(":Boolean");
			return res.toString();
		} else if (STRICT) {
			if (value instanceof String)
				return new StringBuilder("'").append(value).append(":String'").toString();
			else if (value instanceof Integer)
				return new StringBuilder("'").append(value).append(":Integer'").toString();
			else if (value instanceof Long)
				return new StringBuilder("").append(value).append(":Long").toString();
			else if (value instanceof Float)
				return new StringBuilder("").append(value).append(":Float").toString();
			else if (value instanceof Double)
				return new StringBuilder("").append(value).append(":Double").toString();
			else if (value instanceof Boolean) {
				if (((Boolean) value).booleanValue())
					return "true:Boolean";
				return "false:Boolean";
			} else if (value instanceof Character)
				return new StringBuilder().append(value).append(":Char").toString();
			else
				return value.toString();
		} else if (value instanceof String)
			return (String) value;
		else if (value instanceof Integer)
			return ((Integer) value).toString();
		else if (value instanceof Long)
			return new StringBuilder("").append(value).append("l").toString();
		else if (value instanceof Float)
			return new StringBuilder("").append(value).append("f").toString();
		else if (value instanceof Double)
			return new StringBuilder("").append(value).append("d").toString();
		else if (value instanceof Boolean) {
			if (((Boolean) value).booleanValue())
				return "true";
			return "false";
		} else if (value instanceof Character)
			return new StringBuilder("'").append(value).append("'").toString();
		else
			return value.toString();
	}

	/**
	 * custom type converts without back transscription support
	 *
	 * @author Xyan
	 *
	 */
	public static interface ITypeConverter<T> {
		/**
		 * gets an string has to deliver an object
		 *
		 * @param str
		 * @return
		 */
		public T parse(String str);
	}
}