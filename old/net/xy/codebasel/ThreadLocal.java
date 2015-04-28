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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ustomized threadlocal object
 * 
 * @author Xyan
 * 
 */
public class ThreadLocal {
    /**
     * store thread value tables
     */
    private static final Map THREADMAP = Collections.synchronizedMap(new HashMap());

    /**
     * sets value to actual thread
     * 
     * @param obj
     */
    public static void set(final Object obj) {
        THREADMAP.put(Integer.valueOf(Thread.currentThread().hashCode()), obj);
    }

    /**
     * gets value from actual thread
     * 
     * @param obj
     * @return
     */
    public static Object get() {
        return THREADMAP.get(Integer.valueOf(Thread.currentThread().hashCode()));
    }

    /**
     * sets value to given thread
     * 
     * @param obj
     */
    public static void set(final Object obj, final Thread th) {
        if (th != null) {
            THREADMAP.put(Integer.valueOf(th.hashCode()), obj);
        }
    }

    /**
     * gets value from given thread
     * 
     * @param obj
     * @return
     */
    public static Object get(final Thread th) {
        if (th != null) {
            return THREADMAP.get(Integer.valueOf(th.hashCode()));
        }
        return null;
    }
}
