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