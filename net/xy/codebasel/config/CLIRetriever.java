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

import java.util.HashMap;
import java.util.Map;

import net.xy.codebasel.config.Config.IConfigRetriever;

/**
 * retrieves values from the initial commandline
 * 
 * @author xyan
 * 
 */
public class CLIRetriever implements IConfigRetriever {
    /**
     * holds the config
     */
    private static final Map ARGS = new HashMap();

    /**
     * uses prior parsed args if present
     */
    public CLIRetriever() {

    }

    /**
     * inits with args
     * 
     * @param args
     */
    public CLIRetriever(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            final String val = args[i];
            final String[] parts;
            if (val.contains("=")) {
                parts = val.split("=", 2);
            } else if (val.contains(":")) {
                parts = val.split(":", 2);
            } else {
                CLIRetriever.ARGS.put(Integer.valueOf(i), val);
                continue;
            }
            String key = parts[0];
            while (key.startsWith("-")) {
                key = key.substring(1);
            }
            CLIRetriever.ARGS.put(key, parts[1].trim());
        }
    }

    public Object load(final String key) {
        return ARGS.get(key.startsWith("cli:") ? key.substring(4) : key);
    }
}