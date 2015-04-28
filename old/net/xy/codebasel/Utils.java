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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * utility methods
 * 
 * @author Xyan
 * 
 */
public class Utils {

    /**
     * convience method for suspending the current thread
     * 
     * @param milliseconds
     */
    public static void sleep(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException e) {
        }
    }

    /**
     * convience method for waiting upon an latch
     * 
     * @param latch
     */
    public static void await(final CountDownLatch latch) {
        try {
            latch.await();
        } catch (final InterruptedException e) {
        }
    }

    /**
     * gets all declared fields of an class and its super in alphabetical order
     * 
     * @param target
     * @return
     */
    public static List getFields(final Class target) {
        final List fields = new ArrayList();
        final Class cl = target;
        fields.addAll(Arrays.asList(cl.getDeclaredFields()));
        Class pcl = cl.getSuperclass();
        while (pcl != null) {
            fields.addAll(Arrays.asList(pcl.getDeclaredFields()));
            pcl = pcl.getSuperclass(); // next
        }
        Collections.sort(fields, new Comparator() {
            public int compare(final Object f1, final Object f2) {
                return ((Field) f1).getName().compareTo(((Field) f1).getName());
            }
        });
        return fields;
    }

    /**
     * convience method add and returns the given object
     * 
     * @param obj
     * @param list
     * @return
     */
    public static Object returnAdd(final Object obj, final List list) {
        list.add(obj);
        return obj;
    }
}
