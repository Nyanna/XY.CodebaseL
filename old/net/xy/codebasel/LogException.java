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

import net.xy.codebasel.config.Cfg;
import net.xy.codebasel.config.Cfg.Config;

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
    public LogException(final Config messageKey) {
        super(Cfg.string(messageKey));
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogException(final Config messageKey, final Throwable cause) {
        super(Cfg.string(messageKey), cause);
        setStackTrace(cause.getStackTrace());
    }

    /**
     * uses additional objects for tracing values
     * 
     * @param messageKey
     * @param cause
     * @param additional
     */
    public LogException(final Config messageKey, final Throwable cause, final Object[] additional) {
        super(Debug.values(Cfg.string(messageKey), additional), cause);
        setStackTrace(cause.getStackTrace());
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogException(final Config messageKey, final Object[] additional) {
        super(Debug.values(Cfg.string(messageKey), additional));
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