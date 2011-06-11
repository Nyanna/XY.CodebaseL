package net.xy.codebasel;

import net.xy.codebasel.config.Config.ConfigKey;

public class LogWarning extends LogException {
    private static final long serialVersionUID = 4654414387748808062L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogWarning(final ConfigKey messageKey) {
        super(messageKey);
        Log.warning(messageKey);
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogWarning(final ConfigKey messageKey, final Throwable cause) {
        super(messageKey, cause);
        Log.log(Log.LVL_WARNING, toString(), null);
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogWarning(final ConfigKey messageKey, final Throwable cause, final Object[] additional) {
        super(messageKey, cause, additional);
        Log.log(Log.LVL_WARNING, toString(), additional);
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogWarning(final ConfigKey messageKey, final Object[] additional) {
        super(messageKey, additional);
        Log.log(Log.LVL_WARNING, toString(), additional);
    }
}