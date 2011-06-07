package net.xy.codebasel;

import java.util.Arrays;

import net.xy.codebasel.config.Config;
import net.xy.codebasel.config.Config.ConfigKey;

/**
 * wrapper for various logging solutions
 * 
 * @author Xyan
 * 
 */
public class Log {
    /**
     * application reaches broken code
     */
    public static final Level LVL_FATTAL = new Level(1, Config.registerValues("debug.lvl.name.1",
            "FATA"));
    /**
     * db connection abborted using cached images
     */
    public static final Level LVL_CRITICAL = new Level(2, Config.registerValues("debug.lvl.name.2",
            "CRIT"));
    /**
     * failure on loading 1 image
     */
    public static final Level LVL_ERROR = new Level(3, Config.registerValues("debug.lvl.name.3",
            "EROR"));
    /**
     * error in parsing image aspects
     */
    public static final Level LVL_WARNING = new Level(4, Config.registerValues("debug.lvl.name.4",
            "WARN"));
    /**
     * image aspect got recalculates
     */
    public static final Level LVL_NOTICE = new Level(5, Config.registerValues("debug.lvl.name.5",
            "NOTI"));
    /**
     * parsing of 22 pics successful
     */
    public static final Level LVL_COMMENT = new Level(6, Config.registerValues("debug.lvl.name.6",
            "COMM"));
    /**
     * image loader with tiff and jped support loaded in version
     */
    public static final Level LVL_TRACE = new Level(7, Config.registerValues("debug.lvl.name.7",
            "TRAC"));
    /**
     * starting image parsing at byte with speed
     */
    public static final Level LVL_MISC = new Level(8, Config.registerValues("debug.lvl.name.8",
            "MISC"));
    /**
     * application starts or ends
     */
    public static final Level LVL_ALL = new Level(9, Config.registerValues("debug.lvl.name.9",
            "ALLL"));
    /**
     * config keys
     */
    private static final ConfigKey CONFIG_TRACELOG_LEVEL = Config.registerValues(
            "debug.tracelog.level", LVL_WARNING.num);
    private static final ConfigKey CONFIG_ERROROUT_LEVEL = Config.registerValues(
            "debug.stout.errorout.level", LVL_WARNING.num);

    /**
     * class indicating error level
     * 
     * @author xyan
     * 
     */
    public static class Level {
        private final Integer num;
        private final ConfigKey name;

        private Level(final int num, final ConfigKey name) {
            this.num = Integer.valueOf(num);
            this.name = name;
        }
    }

    /**
     * loglistener default logs to system.out and error
     */
    public static ILogListener listener = new ILogListener() {
        public boolean log(final Level level, final String message, final StackTraceElement[] stack) {
            final String messageLine = new StringBuilder("[").append(Config.getString(level.name))
                    .append("] ").append(message).toString();
            if (level.num.intValue() > Config.getInteger(CONFIG_ERROROUT_LEVEL).intValue()) {
                System.out.println(messageLine);
            } else {
                System.err.println(messageLine);
            }
            return true;
        }
    };

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean fattal(final ConfigKey key) {
        return fattal(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean fattal(final ConfigKey key, final Object[] objs) {
        return log(LVL_FATTAL, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean critical(final ConfigKey key) {
        return critical(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean critical(final ConfigKey key, final Object[] objs) {
        return log(LVL_CRITICAL, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean error(final ConfigKey key) {
        return error(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean error(final ConfigKey key, final Object[] objs) {
        return log(LVL_ERROR, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean warning(final ConfigKey key) {
        return warning(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean warning(final ConfigKey key, final Object[] objs) {
        return log(LVL_WARNING, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean notice(final ConfigKey key) {
        return notice(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean notice(final ConfigKey key, final Object[] objs) {
        return log(LVL_NOTICE, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean comment(final ConfigKey key) {
        return comment(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean comment(final ConfigKey key, final Object[] objs) {
        return log(LVL_COMMENT, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean trace(final ConfigKey key) {
        return trace(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean trace(final ConfigKey key, final Object[] objs) {
        return log(LVL_TRACE, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean misc(final ConfigKey key) {
        return misc(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean misc(final ConfigKey key, final Object[] objs) {
        return log(LVL_MISC, key, objs);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean all(final ConfigKey key) {
        return all(key, null);
    }

    /**
     * log relay
     * 
     * @param key
     *            message key to ask config for
     * @return
     */
    public static final boolean all(final ConfigKey key, final Object[] objs) {
        return log(LVL_ALL, key, objs);
    }

    /**
     * log an message
     * 
     * @param level
     *            errorlevel see constants
     * @param key
     *            of message to ask config for
     * @param objs
     *            additional log objects
     * @return if this loglevel was enabled to calculate/add additional logdata
     */
    private static final boolean log(final Level level, final ConfigKey key, final Object[] objs) {
        if (listener == null) {
            return false;
        }
        Throwable exception = null; // look for exception
        if (objs != null) {
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] instanceof Throwable) {
                    exception = (Throwable) objs[i];
                    break;
                }
            }
        }

        if (level.num.intValue() > Config.getInteger(CONFIG_TRACELOG_LEVEL).intValue()
                && exception == null) {
            return listener.log(level, Debug.values(Config.getString(key), objs), null);
        } else {
            final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int i = 0;
            for (; i < trace.length; i++) {
                if (!trace[i].getClassName().equals(Log.class.getName())) {
                    break;
                }
            }
            final boolean ret = listener.log(level, Debug.values(Config.getString(key), objs),
                    (StackTraceElement[]) Arrays.copyOfRange(trace, i, trace.length));
            if (exception != null) {
                throw new RuntimeException(exception);
            }
            return ret;
        }
    }

    /**
     * listener to adapt an logging
     * 
     * @author xyan
     * 
     */
    public static interface ILogListener {
        /**
         * logs an message on these level with these stack
         * 
         * @param level
         * @param message
         * @param stack
         * @return
         */
        public boolean log(Level level, String message, StackTraceElement[] stack);
    }
}
