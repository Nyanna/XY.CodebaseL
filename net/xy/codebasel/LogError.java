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