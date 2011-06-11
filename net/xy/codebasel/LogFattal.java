package net.xy.codebasel;

import net.xy.codebasel.config.Config.ConfigKey;

public class LogFattal extends LogException {
    private static final long serialVersionUID = 3389979592493914382L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogFattal(final ConfigKey messageKey) {
        super(messageKey);
        Log.fattal(messageKey);
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogFattal(final ConfigKey messageKey, final Throwable cause) {
        super(messageKey, cause);
        Log.log(Log.LVL_FATTAL, toString(), null);
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogFattal(final ConfigKey messageKey, final Throwable cause, final Object[] additional) {
        super(messageKey, cause, additional);
        Log.log(Log.LVL_FATTAL, toString(), additional);
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogFattal(final ConfigKey messageKey, final Object[] additional) {
        super(messageKey, additional);
        Log.log(Log.LVL_FATTAL, toString(), additional);
    }
}