package net.xy.codebasel.config;

import net.xy.codebasel.config.Config.IConfigRetriever;

/**
 * retrieves config from local environment vars
 * 
 * @author xyan
 * 
 */
public class EnvarRetriever implements IConfigRetriever {

    public Object load(final String key) {
        return System.getenv(key.startsWith("env:") ? key.substring(4) : key);
    }
}
