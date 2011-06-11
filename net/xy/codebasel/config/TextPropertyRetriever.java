package net.xy.codebasel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import net.xy.codebasel.TypeConverter;
import net.xy.codebasel.config.Config.IConfigRetriever;

/**
 * implements the retriever for text properties
 * 
 * @author Xyan
 * 
 */
public class TextPropertyRetriever implements IConfigRetriever {
    /**
     * stores the properties
     */
    private final Properties prop = new Properties();

    /**
     * init by stream
     * 
     * @param inStream
     * @throws IOException
     */
    public TextPropertyRetriever(final InputStream inStream) throws IOException {
        prop.load(inStream);
    }

    /**
     * init by reader
     * 
     * @param reader
     * @throws IOException
     */
    public TextPropertyRetriever(final Reader reader) throws IOException {
        prop.load(reader);
    }

    /**
     * init by file
     * 
     * @param file
     * @throws IOException
     * @throws FileNotFoundException
     */
    public TextPropertyRetriever(final File file) throws FileNotFoundException, IOException {
        prop.load(new FileInputStream(file));
    }

    /**
     * init ba resource, maybe from package
     * 
     * @param resourceName
     * @throws IOException
     */
    public TextPropertyRetriever(final String resourceName) throws IOException {
        prop.load(TextPropertyRetriever.class.getClassLoader().getResourceAsStream(resourceName));
    }

    public Object load(final String key) {
        return TypeConverter.string2type(prop.getProperty(key.startsWith("property:") ? key
                .substring(7) : key));
    }
}