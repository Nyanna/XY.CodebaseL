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
package net.xy.codebase.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * main class for efficient serialization with minimal overhead
 *
 * @author Xyan
 *
 */
public class SerializationContext {
	/**
	 * class index for numerical enumeration
	 */
	public final List<Class<?>> classes;

	// TODO make performancetest java serial versus own
	// TODO [last] improved support of arrays of the same type
	// TODO [last] reduce type data to byte, possibly different contexts should
	// be used if more is requiered

	/**
	 * constructs the class index
	 *
	 * @param classes
	 */
	public SerializationContext(final Class<?>[] classes) {
		final List<Class<?>> list = Arrays.asList(classes);
		classes: for (final Class<?> class1 : list) {
			final Class<?> cl = class1;
			if (!cl.isInterface() && !cl.isInstance(Serializable.class)) {
				Class<?> pcl = cl.getSuperclass();
				while (pcl != null) {
					if (pcl.isInstance(Serializable.class) || cl == Serializable.class)
						continue classes;
					pcl = cl.getSuperclass();
				}
				throw new IllegalArgumentException("Class doesn't implements serializable [" + cl + "]");
			}
		}
		Collections.sort(list, (c1, c2) -> c1.getSimpleName().compareTo(c2.getSimpleName()));
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
	private void write(final DataOutputStream out, final Object target) throws IOException, IllegalArgumentException,
			IllegalAccessException {
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
			// Array
			final Class<?> compClass = target.getClass().getComponentType();
			final int aeid = getEid(compClass);
			out.writeShort(aeid); // write type idendifier

			final int alength = Array.getLength(target);
			out.writeInt(alength); // write length
			if (alength > 0)
				for (int ac = 0; ac < alength; ac++)
					write(out, Array.get(target, ac));
			break;
		case -9:
			// Byte
			out.writeByte(((Byte) target).intValue());
			break;
		case -10:
			// Enum val
			out.writeInt(((Enum<?>) target).ordinal());
			break;
		case -11:
			break;
		default:
			// recursive object
			final List<Field> fields = getFields(target.getClass());
			for (final Field field : fields)
				if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
					field.setAccessible(true);
					write(out, field.get(target));
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
	private Object read(final DataInputStream in) throws IllegalArgumentException, IllegalAccessException, IOException,
			InstantiationException, ClassNotFoundException {
		final short type;
		try {
			type = in.readShort();
		} catch (final EOFException e) {
			return null;
		}
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
			// Array
			final short atype = in.readShort();
			final Class<?> comp = classes.get(atype);
			final int alength = in.readInt();
			if (alength > 0) {
				final Object[] array = new Object[alength];
				for (int ac = 0; ac < alength; ac++)
					array[ac] = read(in);
				final Object res = Array.newInstance(comp, alength);
				System.arraycopy(array, 0, res, 0, alength);
				return res;
			} else
				return null;
		case -9:
			// Byte
			return Byte.valueOf(in.readByte());
		case -10:
			// Enum val
			return Integer.valueOf(in.readInt());
		case -11:
			// object
			return new Object();
		default:
			// recursive object
			final Class<?> cl = classes.get(type);
			final Object target = cl.newInstance();
			final List<Field> fields = getFields(cl);
			for (final Field field : fields)
				if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
					field.setAccessible(true);
					if (!field.isEnumConstant())
						field.set(target, read(in));
					else {
						final Object enun = field.getType().getEnumConstants()[(Integer) read(in)];
						field.set(target, enun);
					}
					field.setAccessible(false);
				}
			return target;
		}
	}

	/**
	 * gets all declared fields of an class and its super in alphabetical order
	 *
	 * @param target
	 * @return
	 */
	public static List<Field> getFields(final Class<?> target) {
		final List<Field> fields = new ArrayList<Field>();
		final Class<?> cl = target;
		fields.addAll(Arrays.asList(cl.getDeclaredFields()));
		Class<?> pcl = cl.getSuperclass();
		while (pcl != null) {
			fields.addAll(Arrays.asList(pcl.getDeclaredFields()));
			pcl = pcl.getSuperclass(); // next
		}
		Collections.sort(fields, (f1, f2) -> f1.getName().compareTo(f1.getName()));
		return fields;
	}

	/**
	 * returns numerical index for context class
	 *
	 * @param clazz
	 * @return
	 */
	private int getEid(final Class<?> clazz) {
		if (clazz.isArray())
			return -8;
		else if (clazz == Short.class)
			return -1;
		else if (clazz == Integer.class)
			return -2;
		else if (clazz == Long.class)
			return -3;
		else if (clazz == Float.class)
			return -4;
		else if (clazz == Double.class)
			return -5;
		else if (clazz == String.class)
			return -6;
		else if (clazz == Character.class)
			return -7;
		else if (clazz == Byte.class)
			return -9;
		else if (clazz.isEnum())
			return -10;
		else if (clazz == Object.class)
			return -11;
		int c = 0;
		for (final Object element : classes) {
			final Class<?> co = (Class<?>) element;
			if (co == clazz)
				return c;
			c++;
		}
		throw new IllegalStateException("Class not in context [" + clazz + "]");
	}
}