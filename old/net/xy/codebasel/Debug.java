/**
 * This file is part of XY.JCms, Copyright 2010 (C) Xyan Kruse, Xyan@gmx.net, Xyan.kilu.de
 * 
 * XY.JCms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * XY.JCms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with XY.JCms. If not, see <http://www.gnu.org/licenses/>.
 */
package net.xy.codebasel;

/**
 * helper class with various methods used for debugging and rendering
 * diagnostics.
 * 
 * @author xyan
 * 
 */
public class Debug {

    /**
     * concatenates various object to be proper displayed on console or vice
     * versa
     * 
     * @param args
     * @return value
     */
    public static String fields(final Object[] args) {
        final StringBuilder ret = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            final Object entry = args[i];
            if (entry == null) {
                continue;
            }
            ret.append("[").append(entry.getClass().getSimpleName()).append("=").append(entry.toString()).append("]");
        }
        return ret.toString();
    }

    /**
     * prints values in common format of "Message was wrong [*message*]"
     * 
     * @param message
     * @param args
     * @return
     */
    public static String values(final String message, final Object[] args) {
        final StringBuilder ret = new StringBuilder().append(message).append(" ");
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                final Object entry = args[i];
                if (entry == null) {
                    continue;
                }
                ret.append("[").append(entry.toString()).append("]");
            }
        }
        return ret.toString();
    }
}
