package net.xy.codebasel.parser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * implements convience methods for common parsing
 * 
 * @author Xyan
 * 
 */
public class AbstractStaxParser {
    /**
     * holds an reference to the corresponding reader
     */
    private XMLStreamReader reader;

    /**
     * interceptor which shifts the reader with next
     * 
     * @param reader
     * @throws XMLStreamException
     */
    protected boolean next() throws XMLStreamException {
        if (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
            reader.next();
            return true;
        }
        return false;
    }

    /**
     * calls the listener for each tag until EOF and ignores childs, stacking
     * needed
     * 
     * @param reader
     * @param listener
     * @throws XMLStreamException
     */
    protected void foreach(final IFoundCall listener) throws XMLStreamException {
        toptag: while (next()) {
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                final QName qname = reader.getName();
                listener.tag();
                while (next()) {
                    if (reader.getEventType() == XMLStreamConstants.END_ELEMENT
                            && reader.getName().equals(qname)) {
                        continue toptag;
                    }
                }
            } else if (reader.getEventType() == XMLStreamConstants.CDATA) {
                listener.cData();
            } else if (reader.getEventType() == XMLStreamConstants.CHARACTERS) {
                listener.charachters();
            } else if (reader.getEventType() == XMLStreamConstants.COMMENT) {
                listener.coment();
            }
        }
    }

    /**
     * reads an raw attribute from reader, throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected String attribute(final String name) {
        return reader.getAttributeValue(null, name);
    }

    // type conversion
    /**
     * reads an string attribute,throws IllegalStateException
     */
    protected String strval(final String name) {
        return attribute(name);
    }

    /**
     * reads an integer,throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected Integer intval(final String name) {
        final String res = attribute(name);
        return res != null ? Integer.valueOf(res) : null;
    }

    /**
     * reads an float,throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected Float floatval(final String name) {
        final String res = attribute(name);
        return res != null ? Float.valueOf(res) : null;
    }

    /**
     * reads an double,throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected Double doubleval(final String name) {
        final String res = attribute(name);
        return res != null ? Double.valueOf(res) : null;
    }

    /**
     * reads an boolean,throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected Boolean boolval(final String name) {
        final String res = attribute(name);
        return res != null ? Boolean.valueOf(res) : null;
    }

    /**
     * reads an integer list in form "1,224,5435",throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected List intlist(final String name) {
        final List res = new ArrayList();
        final String attrib = attribute(name);
        if (attrib != null) {
            final String[] vals = attrib.split(",");
            for (int i = 0; i < vals.length; i++) {
                res.add(Integer.valueOf(vals[i].trim()));
            }
        }
        return res;
    }

    /**
     * reads simple comma separated stringlist,throws IllegalStateException
     * 
     * @param name
     * @return
     */
    protected List strlist(final String name) {
        final List res = new ArrayList();
        final String attrib = attribute(name);
        if (attrib != null) {
            final String[] vals = attrib.split(",");
            for (int i = 0; i < vals.length; i++) {
                res.add(vals[i].trim());
            }
        }
        return res;
    }

    /**
     * returns obmitted default if in is null
     * 
     * @param in
     * @param def
     * @return
     */
    protected Object def(final Object in, final Object def) {
        if (in != null) {
            return in;
        }
        return def;
    }

    /**
     * checks if the actual tag is equals to the given localpart name,throws
     * IllegalStateException
     * 
     * @param name
     * @return
     */
    protected boolean isTag(final String name) {
        return name.equals(reader.getName().getLocalPart());
    }

    /**
     * parser calls listener if something interesting was found
     * 
     * @author Xyan
     * 
     */
    protected static interface IFoundCall {
        public void tag() throws XMLStreamException;

        public void cData() throws XMLStreamException;

        public void charachters() throws XMLStreamException;

        public void coment() throws XMLStreamException;
    }

    /**
     * adapter implements all interface methods, use by easily overwrite methods
     * 
     * @author Xyan
     * 
     */
    protected static class Found implements IFoundCall {
        public void tag() throws XMLStreamException {
        }

        public void cData() throws XMLStreamException {
        }

        public void charachters() throws XMLStreamException {
        }

        public void coment() throws XMLStreamException {
        }
    }

    /**
     * gets the actual reader
     * 
     * @return
     */
    public XMLStreamReader getReader() {
        return reader;
    }

    /**
     * set the actual reader
     * 
     * @param reader
     */
    public void setReader(final XMLStreamReader reader) {
        this.reader = reader;
    }
}