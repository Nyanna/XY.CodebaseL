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
package net.xy.codebasel.config;

import net.xy.codebasel.config.Config.IConfigRetriever;

/**
 * retrieves system properties
 * 
 * @author xyan
 * 
 */
public class SystemPropertyRetriever implements IConfigRetriever {

    public Object load(final String key) {
        return System.getProperty(key.startsWith("system:") ? key.substring(7) : key);
    }
}