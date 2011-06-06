package net.xy.codebasel.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    /**
     * retrieverlist
     */
    private static final List retriever = new ArrayList();

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
        addRetriever(new CLIRetriever(args));
        addRetriever(new EnvarRetriever());
        addRetriever(new SystemPropertyRetriever());
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
        Object value = null;
        for (final Iterator iterator = retriever.iterator(); iterator.hasNext();) {
            final IConfigRetriever retriever = (IConfigRetriever) iterator.next();
            value = retriever.load(key);
        }
        if (value != null && defaultValue != null && value.getClass() != defaultValue.getClass()) {
            throw new IllegalStateException(Debug.values(
                    "Default value and retrieved values have differing types", new Object[] { key,
                            defaultValue, value }));
        }
        setValueInner(keyo, value != null ? value : defaultValue);
        return keyo;
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
        if (strings.containsKey(key) || integers.containsKey(key) || floats.containsKey(key)
                || doubles.containsKey(key) || booleans.containsKey(key)
                || objects.containsKey(key) || lists.containsKey(key) || maps.containsKey(key)) {
            return true;
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

        /**
         * private constructor
         * 
         * @param key
         */
        public ConfigKey(final String key) {
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