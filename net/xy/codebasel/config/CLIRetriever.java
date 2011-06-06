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
    private final Map args = new HashMap();

    public CLIRetriever(final String[] args) {
        for (int i = 0; i < args.length; i++) {
            final String val = args[i];
            final String[] parts;
            if (val.contains("=")) {
                parts = val.split("=", 2);
            } else if (val.contains(":")) {
                parts = val.split(":", 2);
            } else {
                this.args.put(Integer.valueOf(i), val);
                continue;
            }
            String key = parts[0];
            while (key.startsWith("-")) {
                key = key.substring(1);
            }
            this.args.put(key, parts[1].trim());
        }
    }

    public Object load(final String key) {
        return args.get(key.startsWith("cli:") ? key.substring(4) : key);
    }
}