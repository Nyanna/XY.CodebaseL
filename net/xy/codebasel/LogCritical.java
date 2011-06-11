package net.xy.codebasel;

import net.xy.codebasel.config.Config.ConfigKey;

public class LogCritical extends LogException {
    private static final long serialVersionUID = -6122183209120976824L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogCritical(final ConfigKey messageKey) {
        super(messageKey);
        Log.critical(messageKey);
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogCritical(final ConfigKey messageKey, final Throwable cause) {
        super(messageKey, cause);
        Log.log(Log.LVL_CRITICAL, toString(), null);
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogCritical(final ConfigKey messageKey, final Throwable cause, final Object[] additional) {
        super(messageKey, cause, additional);
        Log.log(Log.LVL_CRITICAL, toString(), additional);
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogCritical(final ConfigKey messageKey, final Object[] additional) {
        super(messageKey, additional);
        Log.log(Log.LVL_CRITICAL, toString(), additional);
    }
}