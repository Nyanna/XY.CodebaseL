package net.xy.codebasel;

import net.xy.codebasel.config.Config;
import net.xy.codebasel.config.Config.ConfigKey;

/**
 * exception connected to the logging system
 * 
 * @author xyan
 * 
 */
public abstract class LogException extends Error {
    private static final long serialVersionUID = 7945268659162420911L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogException(final ConfigKey messageKey) {
        super(Config.getString(messageKey));
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogException(final ConfigKey messageKey, final Throwable cause) {
        super(Config.getString(messageKey), cause);
        setStackTrace(cause.getStackTrace());
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogException(final ConfigKey messageKey, final Throwable cause, final Object[] additional) {
        super(Debug.values(Config.getString(messageKey), additional), cause);
        setStackTrace(cause.getStackTrace());
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogException(final ConfigKey messageKey, final Object[] additional) {
        super(Debug.values(Config.getString(messageKey), additional));
    }

    public String toString() {
        final String clazz;
        final String message;
        if (getCause() != null) {
            clazz = getCause().getClass().getName();
            if (getMessage() != null) {
                if (getCause().getMessage() != null) {
                    message = getMessage() + ", " + getCause().getMessage();
                } else {
                    message = getMessage();
                }
            } else {
                message = getCause().getMessage();
            }
        } else {
            clazz = getClass().getName();
            message = getMessage();
        }
        return message != null ? clazz + ": " + message : clazz;
    }
}