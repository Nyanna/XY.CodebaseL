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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utility mainly for string to type conversion
 *
 * @author Xyan
 *
 */
public class TypeConverter {
	private final int MOD = Pattern.CASE_INSENSITIVE;
	/**
	 * if true disable implecite relaxed checking
	 */
	public boolean STRICT = false;
	// stricts
	private final Pattern PT_STRICT_STRING = Pattern.compile("(.*):String", MOD);
	private final Pattern PT_STRICT_INT = Pattern.compile("([0-9\\-]{1,10}):Integer", MOD);
	private final Pattern PT_STRICT_LONG = Pattern.compile("([0-9\\-l]{1,19}):Long", MOD);
	private final Pattern PT_STRICT_FLOAT = Pattern.compile("([0-9,\\-f]+):Float", MOD);
	private final Pattern PT_STRICT_DOUBLE = Pattern.compile("([0-9.,\\-d]+):Double", MOD);
	private final Pattern PT_STRICT_BOOL = Pattern.compile("(true|false):Boolean", MOD);
	private final Pattern PT_STRICT_CHAR = Pattern.compile("(.{1}):Char", MOD);
	private final Pattern PT_STRING = Pattern.compile("(\".*\")", MOD);
	private final Pattern PT_INT = Pattern.compile("([0-9\\-]{1,10})", MOD);
	private final Pattern PT_LONG = Pattern.compile("([0-9\\-l]{1,19})", MOD);
	private final Pattern PT_FLOAT = Pattern.compile("([0-9.,\\-f]+)", MOD);
	private final Pattern PT_DOUBLE = Pattern.compile("([0-9.,\\-d]+)", MOD);
	private final Pattern PT_BOOL = Pattern.compile("(true|false)", MOD);
	private final Pattern PT_CHAR = Pattern.compile("('.{1}')", MOD);
	// calls an converter like factory method accepting string returning object
	// private final Pattern PT_CONVERTER =
	// Pattern.compile("\\[(.*)\\]:([a-zA-Z0-9.$]+)", MOD);
	// creates via reflection an instance with an string constructor
	// private final Pattern PT_CUSTOM =
	// Pattern.compile("(.*):([a-zA-Z0-9.$]+)", MOD);
	// returns as lists
	// TODO implement converters and custom type support
	// TODO [9] support for string maps, check XY.Cms
	private final Pattern PT_ARRAY_STRING_SIMPLE = Pattern.compile("\\{(.*)\\}", MOD);
	private final Pattern PT_ARRAY_STRING = Pattern.compile("\\{(.*)\\}:String", MOD);
	private final Pattern PT_ARRAY_INT = Pattern.compile("\\{(.*)\\}:Integer", MOD);
	private final Pattern PT_ARRAY_LONG = Pattern.compile("\\{(.*)\\}:Long", MOD);
	private final Pattern PT_ARRAY_FLOAT = Pattern.compile("\\{(.*)\\}:Float", MOD);
	private final Pattern PT_ARRAY_DOUBLE = Pattern.compile("\\{(.*)\\}:Double", MOD);
	private final Pattern PT_ARRAY_BOOL = Pattern.compile("\\{(.*)\\}:Boolean", MOD);

	// calls an converter accepting string array returning object list
	// private final Pattern PT_ARRAY_CONVERTER =
	// Pattern.compile("\\{(.*)\\}:([a-zA-Z0-9.$]+)", MOD);

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
		} catch (final ClassNotFoundException e) {
		}
		return null;
	}

	/**
	 * Converts an string to its java counterpart TODO remove from XY.Jcms
	 *
	 * @param loader
	 *            for custom types
	 * @throws ClassNotFoundException
	 *             if custom type could not be found
	 */
	public Object string2type(String string, final ClassLoader loader) throws ClassNotFoundException {
		if (string == null) {
			return null;
		}
		Matcher match;
		string = string.trim();
		match = PT_STRICT_STRING.matcher(string);
		if (match.matches()) {
			return match.group(1);
		}
		match = PT_STRICT_INT.matcher(string);
		if (match.matches()) {
			return Integer.valueOf(match.group(1));
		}
		match = PT_STRICT_LONG.matcher(string);
		if (match.matches()) {
			return Long.valueOf(match.group(1));
		}
		match = PT_STRICT_FLOAT.matcher(string);
		if (match.matches()) {
			return Float.valueOf(match.group(1).replace(",", ".").replace("f", ""));
		}
		match = PT_STRICT_DOUBLE.matcher(string);
		if (match.matches()) {
			return Double.valueOf(match.group(1).replace(",", "."));
		}
		match = PT_STRICT_BOOL.matcher(string);
		if (match.matches()) {
			return Boolean.valueOf(match.group(1));
		}
		match = PT_STRICT_CHAR.matcher(string);
		if (match.matches()) {
			return Character.valueOf(match.group(1).charAt(0));
		}
		if (!STRICT) {
			match = PT_STRING.matcher(string);
			if (match.matches()) {
				return match.group(1);
			}
			match = PT_INT.matcher(string);
			if (match.matches()) {
				return Integer.valueOf(match.group(1));
			}
			match = PT_LONG.matcher(string);
			if (match.matches()) {
				return Long.valueOf(match.group(1));
			}
			match = PT_FLOAT.matcher(string);
			if (match.matches()) {
				return Float.valueOf(match.group(1).replace(",", ".").replace("f", ""));
			}
			match = PT_DOUBLE.matcher(string);
			if (match.matches()) {
				return Double.valueOf(match.group(1).replace(",", "."));
			}
			match = PT_BOOL.matcher(string);
			if (match.matches()) {
				return Boolean.valueOf(match.group(1));
			}
			match = PT_CHAR.matcher(string);
			if (match.matches()) {
				return Character.valueOf(match.group(1).charAt(0));
			}
			match = PT_ARRAY_STRING_SIMPLE.matcher(string);
			if (match.matches()) {
				return Arrays.asList(match.group(1).split(","));
			}
		}
		match = PT_ARRAY_STRING.matcher(string);
		if (match.matches()) {
			return Arrays.asList(match.group(1).split(";"));
		}
		match = PT_ARRAY_INT.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(";");
			final List<Integer> res = new ArrayList<>();
			for (final String val : vals) {
				res.add(Integer.valueOf(val.trim()));
			}
			return res;
		}
		match = PT_ARRAY_LONG.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(";");
			final List<Long> res = new ArrayList<>();
			for (final String val : vals) {
				res.add(Long.valueOf(val.trim()));
			}
			return res;
		}
		match = PT_ARRAY_FLOAT.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(";");
			final List<Float> res = new ArrayList<>();
			for (final String val : vals) {
				res.add(Float.valueOf(val.trim()));
			}
			return res;
		}
		match = PT_ARRAY_DOUBLE.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(";");
			final List<Double> res = new ArrayList<>();
			for (final String val : vals) {
				res.add(Double.valueOf(val.trim()));
			}
			return res;
		}
		match = PT_ARRAY_BOOL.matcher(string);
		if (match.matches()) {
			final String[] vals = match.group(1).split(";");
			final List<Boolean> res = new ArrayList<>();
			for (final String val : vals) {
				res.add(Boolean.valueOf(val.trim()));
			}
			return res;
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
				if (i > 0) {
					res.append(";");
				}
				res.append(((Object[]) value)[i]);
			}
			res.append("}");
			final Class<?> clazz = value.getClass().getComponentType();
			if (clazz.isAssignableFrom(String.class)) {
				res.append(":String");
			} else if (clazz.isAssignableFrom(Integer.class)) {
				res.append(":Integer");
			} else if (clazz.isAssignableFrom(Long.class)) {
				res.append(":Long");
			} else if (clazz.isAssignableFrom(Float.class)) {
				res.append(":Float");
			} else if (clazz.isAssignableFrom(Double.class)) {
				res.append(":Double");
			} else if (clazz.isAssignableFrom(Boolean.class)) {
				res.append(":Boolean");
			}
			return res.toString();
		} else if (STRICT) {
			if (value instanceof String) {
				return new StringBuilder("'").append(value).append(":String'").toString();
			} else if (value instanceof Integer) {
				return new StringBuilder("'").append(value).append(":Integer'").toString();
			} else if (value instanceof Long) {
				return new StringBuilder("").append(value).append(":Long").toString();
			} else if (value instanceof Float) {
				return new StringBuilder("").append(value).append(":Float").toString();
			} else if (value instanceof Double) {
				return new StringBuilder("").append(value).append(":Double").toString();
			} else if (value instanceof Boolean) {
				if (((Boolean) value).booleanValue()) {
					return "true:Boolean";
				}
				return "false:Boolean";
			} else if (value instanceof Character) {
				return new StringBuilder().append(value).append(":Char").toString();
			} else {
				return value.toString();
			}
		} else {
			if (value instanceof String) {
				return (String) value;
			} else if (value instanceof Integer) {
				return ((Integer) value).toString();
			} else if (value instanceof Long) {
				return new StringBuilder("").append(value).append("l").toString();
			} else if (value instanceof Float) {
				return new StringBuilder("").append(value).append("f").toString();
			} else if (value instanceof Double) {
				return new StringBuilder("").append(value).append("d").toString();
			} else if (value instanceof Boolean) {
				if (((Boolean) value).booleanValue()) {
					return "true";
				}
				return "false";
			} else if (value instanceof Character) {
				return new StringBuilder("'").append(value).append("'").toString();
			} else {
				return value.toString();
			}
		}
	}
}