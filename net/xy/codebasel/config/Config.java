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
package net.xy.codebasel.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.xy.codebasel.Debug;

/**
 * aplication configuration object
 * 
 * @author xyan
 * 
 */
public class Config {
    /**
     * data indices
     */
    private static final Map strings = new HashMap();
    private static final Map integers = new HashMap();
    private static final Map floats = new HashMap();
    private static final Map doubles = new HashMap();
    private static final Map booleans = new HashMap();
    private static final Map objects = new HashMap();
    private static final Map lists = new HashMap();
    private static final Map maps = new HashMap();
    private static final Map[] index = new Map[] { strings, integers, floats, doubles, booleans,
            objects, lists, maps };
    /**
     * retrieverlist
     */
    private static final List retriever = new ArrayList();
    /**
     * toggles always include defaults on reset
     */
    public static boolean includeDefaults = false;

    /**
     * loads all classes and call their static initializers
     * 
     * @param classes
     */
    public static void registerClasses(final Class[] classes) {
        // just loads classes with their static initializers
    }

    /**
     * adds default retrievers in order cli,envar,sysprops
     * 
     * @param args
     */
    public static void addDefaultRetrievers(final String[] args) {
        includeDefaults = true;
        if (args != null) {
            addRetriever(new CLIRetriever(args));
        } else {
            addRetriever(new CLIRetriever());
        }
        addRetriever(new EnvarRetriever());
        addRetriever(new SystemPropertyRetriever());
    }

    /**
     * refrehes all values by passing all retrivers again
     */
    public static void refresh() {
        for (int i = 0; i < index.length; i++) {
            final Map map = index[i];
            for (final Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
                final Entry entry = (Entry) iterator.next();
                readValue((ConfigKey) entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * registers an config value throws IllegalArgument if already registered,
     * would call all config retrievers if there
     * are.
     * 
     * @param key
     * @param defaultValue
     * @return returns the generated key
     */
    public static ConfigKey registerValues(final String key, final Object defaultValue) {
        final ConfigKey keyo = new ConfigKey(key);
        if (isRegistered(keyo, defaultValue != null ? defaultValue.getClass() : Object.class)) {
            throw new IllegalArgumentException(Debug.values("Configkey already registered",
                    new Object[] { key, defaultValue }));
        }
        readValue(keyo, defaultValue);
        return keyo;
    }

    /**
     * inner read checks all retrievers and sets value
     * 
     * @param key
     * @param keyo
     * @param defaultValue
     */
    private static void readValue(final ConfigKey keyo, final Object defaultValue) {
        Object value = null;
        for (final Iterator iterator = retriever.iterator(); iterator.hasNext();) {
            final IConfigRetriever retriever = (IConfigRetriever) iterator.next();
            value = retriever.load(keyo.backup);
            if (value != null) {
                break;
            }
        }
        if (value != null && defaultValue != null && value.getClass() != defaultValue.getClass()) {
            throw new IllegalStateException(Debug.values(
                    "Default value and retrieved values have differing types", new Object[] {
                            keyo.backup, defaultValue, value }));
        }
        setValueInner(keyo, value != null ? value : defaultValue);
    }

    /**
     * gets an string config
     * 
     * @param key
     * @return
     */
    public static String getString(final ConfigKey key) {
        return (String) strings.get(key);
    }

    /**
     * gets an integer config
     * 
     * @param key
     * @return
     */
    public static Integer getInteger(final ConfigKey key) {
        return (Integer) integers.get(key);
    }

    /**
     * gets an float config
     * 
     * @param key
     * @return
     */
    public static Float getFloat(final ConfigKey key) {
        return (Float) floats.get(key);
    }

    /**
     * gets an double config
     * 
     * @param key
     * @return
     */
    public static Double getDouble(final ConfigKey key) {
        return (Double) doubles.get(key);
    }

    /**
     * gets an boolean config
     * 
     * @param key
     * @return
     */
    public static Boolean getBoolean(final ConfigKey key) {
        return (Boolean) booleans.get(key);
    }

    /**
     * gets an custom object config
     * 
     * @param key
     * @return
     */
    public static Object getObject(final ConfigKey key) {
        return objects.get(key);
    }

    /**
     * gets an list config
     * 
     * @param key
     * @return
     */
    public static List getList(final ConfigKey key) {
        return (List) lists.get(key);
    }

    /**
     * gets an map config
     * 
     * @param key
     * @return
     */
    public static Map getMap(final ConfigKey key) {
        return (Map) maps.get(key);
    }

    /**
     * checks if an key is already registered
     * 
     * @param key
     * @param valueType
     * @return
     */
    public static boolean isRegistered(final ConfigKey key, final Class valueType) {
        for (int i = 0; i < index.length; i++) {
            final Map map = index[i];
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * changes an value
     * 
     * @param key
     * @param value
     */
    public static void setValue(final ConfigKey key, final Object value) {
        if (isRegistered(key, value.getClass())) {
            setValueInner(key, value);
        }
        throw new IllegalArgumentException(Debug.values("Configkey not registered", new Object[] {
                key, value }));
    }

    /**
     * internally sets an value
     * 
     * @param key
     * @param value
     */
    private static void setValueInner(final ConfigKey key, final Object value) {
        if (value instanceof String) {
            strings.put(key, value);
        } else if (value instanceof Integer) {
            integers.put(key, value);
        } else if (value instanceof Float) {
            floats.put(key, value);
        } else if (value instanceof Double) {
            doubles.put(key, value);
        } else if (value instanceof Boolean) {
            booleans.put(key, value);
        } else if (value instanceof List) {
            lists.put(key, value);
        } else if (value instanceof Map) {
            maps.put(key, value);
        } else {
            objects.put(key, value);
        }
    }

    /**
     * adds an retriever to the end of the list
     * 
     * @param retriever
     */
    public static void addRetriever(final IConfigRetriever retriever) {
        Config.retriever.add(retriever);
    }

    /**
     * remove retriever
     * 
     * @param retriever
     */
    public static void removeRetriever(final IConfigRetriever retriever) {
        Config.retriever.remove(retriever);
    }

    /**
     * removes all retrievers but reinits defaults if previously were init
     */
    public static void removeAllRetriever() {
        Config.retriever.clear();
        if (includeDefaults) {
            addDefaultRetrievers(null);
        }
    }

    /**
     * config retriever for reading config on demand
     * 
     * @author xyan
     * 
     */
    public static interface IConfigRetriever {
        /**
         * loads an config value from an unspecified source
         * 
         * @param key
         * @return
         */
        public Object load(final String key);
    }

    /**
     * key identifieing an registered config value
     * 
     * @author xyan
     * 
     */
    public static class ConfigKey {
        // holds the initial hashcode
        private final Integer hashkey;
        // backup of the key string for internal use
        private final String backup;

        /**
         * private constructor
         * 
         * @param key
         */
        public ConfigKey(final String key) {
            backup = key;
            hashkey = Integer.valueOf(key.hashCode());
        }

        public int hashCode() {
            return hashkey.intValue();
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof Integer)) {
                return false;
            }
            return hashkey == (Integer) obj;
        }
    }
    // TODO look after string to type converter scheme
}