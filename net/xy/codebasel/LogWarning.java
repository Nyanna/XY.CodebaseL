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

import net.xy.codebasel.config.Cfg.Config;

public class LogWarning extends LogException {
    private static final long serialVersionUID = 4654414387748808062L;

    /**
     * minimal constructor with just a message
     * 
     * @param messageKey
     */
    public LogWarning(final Config messageKey) {
        super(messageKey);
        Log.warning(messageKey);
    }

    /**
     * use the message and an cause
     * 
     * @param messageKey
     * @param cause
     */
    public LogWarning(final Config messageKey, final Throwable cause) {
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
    public LogWarning(final Config messageKey, final Throwable cause, final Object[] additional) {
        super(messageKey, cause, additional);
        Log.log(Log.LVL_WARNING, toString(), additional);
    }

    /**
     * an typeless exception for logging and inheritance usage only
     * 
     * @param messageKey
     * @param additional
     */
    public LogWarning(final Config messageKey, final Object[] additional) {
        super(messageKey, additional);
        Log.log(Log.LVL_WARNING, toString(), additional);
    }
}