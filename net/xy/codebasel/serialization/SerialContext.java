package net.xy.codebasel.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.xy.codebasel.Debug;
import net.xy.codebasel.Utils;

/**
 * main class for efficient serialization with minimal overhead
 * 
 * @author Xyan
 * 
 */
public class SerialContext {
    /**
     * class index for numerical enumeration
     */
    public final List classes;

    // TODO make performancetest java serial versus own

    /**
     * constructs the class index
     * 
     * @param classes
     */
    public SerialContext(final Class[] classes) {
        final List list = Arrays.asList(classes);
        classes: for (final Iterator iterator = list.iterator(); iterator.hasNext();) {
            final Class cl = (Class) iterator.next();
            if (!cl.isInterface() && !cl.isInstance(Serializable.class)) {
                Class pcl = cl.getSuperclass();
                while (pcl != null) {
                    if (pcl.isInstance(Serializable.class) || cl == Serializable.class) {
                        continue classes;
                    }
                    pcl = cl.getSuperclass();
                }
                throw new IllegalArgumentException(Debug.values(
                        "Class doesn't implements serializable", new Object[] { cl }));
            }
        }
        Collections.sort(list, new Comparator() {
            public int compare(final Object c1, final Object c2) {
                return ((Class) c1).getSimpleName().compareTo(((Class) c2).getSimpleName());
            }
        });
        this.classes = Collections.unmodifiableList(list);
    }

    /**
     * serializes an object recursively to the outputstream
     * 
     * @param out
     * @param target
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void serialize(final DataOutputStream out, final Object target) throws IOException,
            IllegalArgumentException, IllegalAccessException {
        write(out, target);
    }

    /**
     * deserializes from input stream
     * 
     * @param in
     * @return
     */
    public Object deserialize(final DataInputStream in) {
        Exception e = null;
        try {
            return read(in);
        } catch (final IllegalArgumentException e1) {
            e = e1;
        } catch (final IllegalAccessException e2) {
            e = e2;
        } catch (final IOException e3) {
            e = e3;
        } catch (final InstantiationException e4) {
            e = e4;
        } catch (final ClassNotFoundException e5) {
            e = e5;
        }
        throw new IllegalStateException("Error on reading object", e);
    }

    /**
     * recursively writes the objecttree
     * 
     * @param out
     * @param target
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private void write(final DataOutputStream out, final Object target) throws IOException,
            IllegalArgumentException, IllegalAccessException {
        final int eid = getEid(target.getClass());
        out.writeShort(eid); // write type idendifier
        switch (eid) {
        case -1:
            out.writeShort(((Short) target).intValue());
            break;
        case -2:
            out.writeInt(((Integer) target).intValue());
            break;
        case -3:
            out.writeLong(((Long) target).longValue());
            break;
        case -4:
            out.writeFloat(((Float) target).floatValue());
            break;
        case -5:
            out.writeDouble(((Double) target).doubleValue());
            break;
        case -6:
            out.writeUTF((String) target); // string
            break;
        case -7:
            out.writeUTF(String.valueOf(target)); // char
            break;
        case -8:
            // List
            out.writeLong(((List) target).size()); // write len
            for (final Iterator iterator = ((List) target).iterator(); iterator.hasNext();) {
                final Object item = iterator.next();
                write(out, item);
            }
            break;
        case -9:
            // Map
            out.writeLong(((Map) target).size()); // write len
            for (final Iterator iterator = ((Map) target).entrySet().iterator(); iterator.hasNext();) {
                final Entry entry = (Entry) iterator.next();
                write(out, entry.getKey());
                write(out, entry.getValue());
            }
            break;
        case -10:
            // Array
            final int alength = Array.getLength(target);
            out.writeInt(alength); // write length
            if (alength > 0) {
                // write type
                Class arrayClass;
                final Class compClass = target.getClass().getComponentType();
                if (compClass.isInterface()) {
                    arrayClass = compClass;
                } else if (compClass.isPrimitive()) {
                    arrayClass = compClass;
                } else if (compClass.isArray()) {
                    arrayClass = Object.class;
                } else {
                    arrayClass = Array.get(target, 0).getClass();
                }
                out.writeUTF(arrayClass.getName());
                for (int ac = 0; ac < alength; ac++) {
                    write(out, Array.get(target, ac));
                }
            }
            break;
        case -11:
            // Byte
            out.writeByte(((Byte) target).intValue());
            break;
        case -12:
            // Enum val
            out.writeInt(((Enum) target).ordinal());
            break;
        default:
            // recursive object
            final List fields = Utils.getFields(target.getClass());
            for (final Iterator iterator = fields.iterator(); iterator.hasNext();) {
                final Field field = (Field) iterator.next();
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    write(out, field.get(target));
                    field.setAccessible(false);
                }
            }
        }
    }

    /**
     * recursively reads an inputstream
     * 
     * @param in
     * @param parent
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private Object read(final DataInputStream in) throws IllegalArgumentException,
            IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
        final short type = in.readShort();
        switch (type) {
        case -1:
            return Short.valueOf(in.readShort());
        case -2:
            return Integer.valueOf(in.readInt());
        case -3:
            return Long.valueOf(in.readLong());
        case -4:
            return Float.valueOf(in.readFloat());
        case -5:
            return Double.valueOf(in.readDouble());
        case -6:
            return in.readUTF();
        case -7:
            return Character.valueOf(in.readUTF().charAt(0)); // char
        case -8:
            // List
            long size = in.readLong();
            final ArrayList read = new ArrayList();
            read.ensureCapacity((int) size);
            for (; size > 0; size--) {
                read.add(read(in));
            }
            return read;
        case -9:
            // Map
            long mapsize = in.readLong();
            final Map readMap = new HashMap((int) mapsize);
            for (; mapsize > 0; mapsize--) {
                readMap.put(read(in), read(in));
            }
            return readMap;
        case -10:
            // Array
            final int alength = in.readInt();
            final Class comp = SerialContext.class.getClassLoader().loadClass(in.readUTF());
            if (alength > 0) {
                final Object[] array = new Object[alength];
                for (int ac = 0; ac < alength; ac++) {
                    array[ac] = read(in);
                }
                final Object res;
                if (comp == Object.class) {
                    res = Array.newInstance(array[0].getClass(), alength);
                } else {
                    res = Array.newInstance(comp, alength);
                }
                System.arraycopy(array, 0, res, 0, alength);
                return res;
            } else {
                return null;
            }
        case -11:
            // Byte
            return Byte.valueOf(in.readByte());
        case -12:
            // Enum val
            return Integer.valueOf(in.readInt());
        default:
            // recursive object
            final Class cl = (Class) classes.get(type);
            final Object target = cl.newInstance();
            final List fields = Utils.getFields(cl);
            for (final Iterator iterator = fields.iterator(); iterator.hasNext();) {
                final Field field = (Field) iterator.next();
                if (!Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    if (!field.isEnumConstant()) {
                        field.set(target, read(in));
                    } else {
                        throw new UnsupportedOperationException(
                                "Readind of enums actually not implemented");
                    }
                    field.setAccessible(false);
                }
            }
            return target;
        }
    }

    /**
     * returns numerical index for context class
     * 
     * @param clazz
     * @return
     */
    private int getEid(final Class clazz) {
        if (clazz.isArray()) {
            return -10;
        } else if (clazz.hashCode() == Short.class.hashCode()) {
            return -1;
        } else if (clazz.hashCode() == Integer.class.hashCode()) {
            return -2;
        } else if (clazz.hashCode() == Long.class.hashCode()) {
            return -3;
        } else if (clazz.hashCode() == Float.class.hashCode()) {
            return -4;
        } else if (clazz.hashCode() == Double.class.hashCode()) {
            return -5;
        } else if (clazz.hashCode() == String.class.hashCode()) {
            return -6;
        } else if (clazz.hashCode() == Character.class.hashCode()) {
            return -7;
        } else if (clazz.isInstance(List.class)) {
            return -8;
        } else if (clazz.isInstance(Map.class)) {
            return -9;
        } else if (clazz.hashCode() == Byte.class.hashCode()) {
            return -11;
        } else if (clazz.isEnum()) {
            return -12;
        }
        int c = 0;
        for (final Iterator iterator = classes.iterator(); iterator.hasNext();) {
            final Class co = (Class) iterator.next();
            if (co == clazz) {
                return c;
            }
            c++;
        }
        throw new IllegalStateException(
                Debug.values("Class not in context", new Object[] { clazz }));
    }
}