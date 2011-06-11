package net.xy.codebasel;

import net.xy.codebasel.config.Config.ConfigKey;

public class LogError extends LogException {
    private static final long serialVersionUID = -4450329390399542749L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogError(final ConfigKey messageKey) {
        super(messageKey);
        Log.error(messageKey);
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogError(final ConfigKey messageKey, final Throwable cause) {
        super(messageKey, cause);
        Log.log(Log.LVL_ERROR, toString(), null);
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogError(final ConfigKey messageKey, final Throwable cause, final Object[] additional) {
        super(messageKey, cause, additional);
        Log.log(Log.LVL_ERROR, toString(), additional);
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogError(final ConfigKey messageKey, final Object[] additional) {
        super(messageKey, additional);
        Log.log(Log.LVL_ERROR, toString(), additional);
    }
}