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