/**
 * This file is part of XY.Codebase, Copyright 2011 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.Codebase is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * XY.Codebase is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with XY.Codebase. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.xy.codebasel;

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
    private static final int MOD = Pattern.CASE_INSENSITIVE;
    /**
     * if true disable implecite relaxed checking
     */
    public static boolean STRICT = false;
    // stricts
    private static final Pattern PT_STRICT_STRING = Pattern.compile("(.*):String", MOD);
    private static final Pattern PT_STRICT_INT = Pattern.compile("([0-9\\-]{1,10}):Integer", MOD);
    private static final Pattern PT_STRICT_LONG = Pattern.compile("([0-9\\-]{1,19}):Long", MOD);
    private static final Pattern PT_STRICT_FLOAT = Pattern.compile("([0-9,\\-f]+):Float", MOD);
    private static final Pattern PT_STRICT_DOUBLE = Pattern.compile("([0-9,\\-d]+):Double", MOD);
    private static final Pattern PT_STRICT_BOOL = Pattern.compile("(true|false):Boolean", MOD);
    private static final Pattern PT_STRICT_CHAR = Pattern.compile("(.{1}):Char", MOD);
    private static final Pattern PT_STRING = Pattern.compile("(\".*\")", MOD);
    private static final Pattern PT_INT = Pattern.compile("([0-9\\-]{1,10})", MOD);
    private static final Pattern PT_LONG = Pattern.compile("([0-9\\-]{1,19})", MOD);
    private static final Pattern PT_FLOAT = Pattern.compile("([0-9,\\-f]+)", MOD);
    private static final Pattern PT_DOUBLE = Pattern.compile("([0-9,\\-d]+)", MOD);
    private static final Pattern PT_BOOL = Pattern.compile("(true|false)", MOD);
    private static final Pattern PT_CHAR = Pattern.compile("('.{1}')", MOD);
    // calls an converter like factory method accepting string returning object
    private static final Pattern PT_CONVERTER = Pattern.compile("\\[(.*)\\]:([a-zA-Z0-9.$]+)", MOD);
    // creates via reflection an instance with an string constructor
    private static final Pattern PT_CUSTOM = Pattern.compile("(.*):([a-zA-Z0-9.$]+)", MOD);
    // returns as lists
    // TODO implement converters and custom type support
    private static final Pattern PT_ARRAY_STRING_SIMPLE = Pattern.compile("\\{(.*)\\}", MOD);
    private static final Pattern PT_ARRAY_STRING = Pattern.compile("\\{(.*)\\}:String", MOD);
    private static final Pattern PT_ARRAY_INT = Pattern.compile("\\{(.*)\\}:Integer", MOD);
    private static final Pattern PT_ARRAY_LONG = Pattern.compile("\\{(.*)\\}:Long", MOD);
    private static final Pattern PT_ARRAY_FLOAT = Pattern.compile("\\{(.*)\\}:Float", MOD);
    private static final Pattern PT_ARRAY_DOUBLE = Pattern.compile("\\{(.*)\\}:Double", MOD);
    private static final Pattern PT_ARRAY_BOOL = Pattern.compile("\\{(.*)\\}:Boolean", MOD);
    // calls an converter accepting string array returning object list
    private static final Pattern PT_ARRAY_CONVERTER = Pattern.compile(
            "\\{(.*)\\}:([a-zA-Z0-9.$]+)", MOD);

    /**
     * delegate without converter and custom type support
     * 
     * @param string
     * @param loader
     * @return
     */
    public static Object string2type(final String string) {
        try {
            return string2type(string, null);
        } catch (final ClassNotFoundException e) {
        }
        return null;
    }

    /**
     * Converts an string to its java counterpart
     * TODO remove from XY.Jcms
     * 
     * @param loader
     *            for custom types
     * @throws ClassNotFoundException
     *             if custom type could not be found
     */
    public static Object string2type(String string, final ClassLoader loader)
            throws ClassNotFoundException {
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
        match = PT_STRICT_LONG.matcher(string);
        if (match.matches()) {
            return Long.valueOf(match.group(1));
        }
        match = PT_STRICT_FLOAT.matcher(string);
        if (match.matches()) {
            return Float.valueOf(match.group(1));
        }
        match = PT_STRICT_DOUBLE.matcher(string);
        if (match.matches()) {
            return Double.valueOf(match.group(1));
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
                return Float.valueOf(match.group(1));
            }
            match = PT_DOUBLE.matcher(string);
            if (match.matches()) {
                return Double.valueOf(match.group(1));
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
            final List res = new ArrayList();
            for (int i = 0; i < vals.length; i++) {
                res.add(Integer.valueOf(vals[i].trim()));
            }
            return res;
        }
        match = PT_ARRAY_LONG.matcher(string);
        if (match.matches()) {
            final String[] vals = match.group(1).split(";");
            final List res = new ArrayList();
            for (int i = 0; i < vals.length; i++) {
                res.add(Long.valueOf(vals[i].trim()));
            }
            return res;
        }
        match = PT_ARRAY_FLOAT.matcher(string);
        if (match.matches()) {
            final String[] vals = match.group(1).split(";");
            final List res = new ArrayList();
            for (int i = 0; i < vals.length; i++) {
                res.add(Float.valueOf(vals[i].trim()));
            }
            return res;
        }
        match = PT_ARRAY_DOUBLE.matcher(string);
        if (match.matches()) {
            final String[] vals = match.group(1).split(";");
            final List res = new ArrayList();
            for (int i = 0; i < vals.length; i++) {
                res.add(Double.valueOf(vals[i].trim()));
            }
            return res;
        }
        match = PT_ARRAY_BOOL.matcher(string);
        if (match.matches()) {
            final String[] vals = match.group(1).split(";");
            final List res = new ArrayList();
            for (int i = 0; i < vals.length; i++) {
                res.add(Boolean.valueOf(vals[i].trim()));
            }
            return res;
        }
        return string;
    }
}