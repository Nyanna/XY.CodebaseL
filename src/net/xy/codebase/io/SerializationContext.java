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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main class for efficient serialization with minimal overhead
 *
 * @author Xyan
 *
 */
public class SerializationContext {
	private static final Logger LOG = LoggerFactory.getLogger(SerializationContext.class);
	/**
	 * class index for numerical enumeration
	 */
	private final Map<Class<?>, Byte> classesToIdx = new HashMap<>();
	private final Map<Byte, Class<?>> idxToClasses = new HashMap<>();
	/*
	 * primitive type constants
	 */
	private static final byte nullEid = 0;
	private static final byte boolTrueEid = 1;
	private static final byte boolFalseEid = 2;
	private static final byte byteEid = 3;
	private static final byte shortEid = 4;
	private static final byte intEid = 5;
	private static final byte longEid = 6;
	private static final byte floatEid = 7;
	private static final byte doubleEid = 8;
	private static final byte charEid = 9;
	private static final byte stringEid = 10;
	private static final byte objEid = 11;
	private static final byte arrayEid = 12;
	private static final byte pBoolTrueEid = 13;
	private static final byte pBoolFalseEid = 14;
	private static final byte pByteEid = 15;
	private static final byte pShortEid = 16;
	private static final byte pIntEid = 17;
	private static final byte pLongEid = 18;
	private static final byte pFloatEid = 19;
	private static final byte pDoubleEid = 20;
	private static final byte pCharEid = 21;

	// this method compresses 1/3 better than java serialization but 2x slower

	/**
	 * constructs the class index
	 *
	 * @param classes
	 */
	public SerializationContext(final Class<?>[]... classes) {
		final byte defCount = defaulTypes();

		if (defCount + classes.length > 254)
			throw new IllegalArgumentException("Contexts class limit exceeded");

		byte idx = defCount;
		for (final Class<?>[] clSet : classes)
			for (final Class<?> cl : clSet) {
				if (cl.isInstance(Serializable.class))
					throw new IllegalArgumentException("Class doesn't implements serializable [" + cl + "]");
				addClass(cl, idx++);
			}
	}

	/**
	 * @return intern id to class mapping
	 */
	public Map<Byte, Class<?>> getClassMap() {
		return idxToClasses;
	}

	/**
	 * method for directly setting a mapping
	 *
	 * @param eid
	 * @param clazz
	 */
	public void setClass(final byte eid, final Class<?> clazz) {
		addClass(clazz, eid);
	}

	/**
	 * directly add an mapping
	 *
	 * @param clazz
	 * @param eid
	 * @return
	 */
	private byte addClass(final Class<?> clazz, final byte eid) {
		classesToIdx.put(clazz, eid);
		idxToClasses.put(eid, clazz);
		return eid;
	}

	/**
	 * inits build in types and primitives
	 *
	 * @return
	 */
	private byte defaulTypes() {
		addClass(Boolean.class, boolTrueEid);
		addClass(Byte.class, byteEid);
		addClass(Short.class, shortEid);
		addClass(Integer.class, intEid);
		addClass(Long.class, longEid);
		addClass(Float.class, floatEid);
		addClass(Double.class, doubleEid);
		addClass(Character.class, charEid);
		addClass(String.class, stringEid);
		addClass(Object.class, objEid);

		// primitives
		addClass(Boolean.TYPE, pBoolTrueEid);
		addClass(Byte.TYPE, pByteEid);
		addClass(Short.TYPE, pShortEid);
		addClass(Integer.TYPE, pIntEid);
		addClass(Long.TYPE, pLongEid);
		addClass(Float.TYPE, pFloatEid);
		addClass(Double.TYPE, pDoubleEid);
		addClass(Character.TYPE, pCharEid);
		return pCharEid + 1;
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
	public void serialize(final DataOutputStream out, final Object target)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		try {
			write(out, target);
		} catch (final IllegalStateException ex) {
			LOG.error("Error serialiting object [" + ex.getMessage() + "]");
			throw ex;
		}
	}

	/**
	 * not throwing variant
	 *
	 * @param out
	 * @param target
	 * @return
	 */
	public boolean serializeCatched(final DataOutputStream out, final Object target) {
		try {
			serialize(out, target);
			return true;
		} catch (IllegalArgumentException | IllegalAccessException | IOException e) {
			LOG.error("Error serialiting object [" + target + "]", e);
		}
		return false;
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
		} catch (final SecurityException e7) {
			e = e7;
		} catch (final InvocationTargetException e8) {
			e = e8;
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
	private void write(final DataOutputStream out, final Object target)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		final byte eid = getEid(target != null ? target.getClass() : null, target);
		out.writeByte(eid); // write type idendifier
		write(out, target, eid);
	}

	/**
	 * writes an concrete type
	 *
	 * @param out
	 * @param target
	 * @param eid
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void write(final DataOutputStream out, final Object target, final byte eid)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		switch (eid) {
		case nullEid:
		case boolTrueEid:
		case boolFalseEid:
		case pBoolTrueEid:
		case pBoolFalseEid:
			break;
		case byteEid:
		case pByteEid:
			out.writeByte(((Byte) target).byteValue());
			break;
		case shortEid:
		case pShortEid:
			out.writeShort(((Short) target).shortValue());
			break;
		case intEid:
		case pIntEid:
			out.writeInt(((Integer) target).intValue());
			break;
		case longEid:
		case pLongEid:
			out.writeLong(((Long) target).longValue());
			break;
		case floatEid:
		case pFloatEid:
			out.writeFloat(((Float) target).floatValue());
			break;
		case doubleEid:
		case pDoubleEid:
			out.writeDouble(((Double) target).doubleValue());
			break;
		case stringEid:
			out.writeUTF((String) target);
			break;
		case pCharEid:
		case charEid:
			out.writeUTF(String.valueOf(target));
			break;
		case objEid:
			break;
		case arrayEid:
			final Class<?> compClass = target.getClass().getComponentType();
			final byte aeid = getClassEid(compClass);
			if (aeid < 0)
				throw new UnserializableException(target);
			out.writeByte(aeid); // write type idendifier
			writeArray(out, target, aeid, compClass);
			break;
		default:
			final Class<?> tcl = target.getClass();
			if (tcl.isEnum())
				out.writeByte(((Enum<?>) target).ordinal());
			else {
				// recursive object
				final List<Field> fields = getFields(tcl);
				for (final Field field : fields)
					if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
						field.setAccessible(true);
						try {
							write(out, field.get(target));
						} catch (final UnserializableException ex) {
							ex.addTarget(target);
							throw ex;
						}
					}
			}
		}
	}

	/**
	 * recursively writes arrays
	 *
	 * @param out
	 * @param target
	 * @param comp
	 * @param aeid
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	private void writeArray(final DataOutputStream out, final Object target, final byte aeid, final Class<?> comp)
			throws IOException, IllegalAccessException {
		final int alength = Array.getLength(target);

		boolean same = true;
		if (alength > 0 && !comp.isPrimitive())
			for (int ac = 0; ac < alength; ac++) {
				final Object val = Array.get(target, ac);
				if (val == null || val.getClass() != comp) {
					same = false;
					break;
				}
			}
		out.writeBoolean(same);

		out.writeInt(alength); // write length
		if (alength > 0)
			for (int ac = 0; ac < alength; ac++) {
				final Object elem = Array.get(target, ac);
				if (same) {
					if (comp.isArray()) {
						final Class<?> compClassO = comp.getComponentType();
						final byte aeidO = getClassEid(comp);
						writeArray(out, elem, aeidO, compClassO);
					} else
						write(out, elem, aeid);
				} else
					write(out, elem);
			}
	}

	/**
	 * encapsulated with check
	 *
	 * @param clazz
	 * @return
	 */
	private byte getClassEid(final Class<?> clazz) {
		final Byte res = classesToIdx.get(clazz);
		if (res == null) {
			LOG.error("Class not in context [" + clazz.getTypeName() + "]");
			return -1;
		}
		return res;
	}

	/**
	 * custom exception to determine not serializable objects in structures
	 *
	 * @author Xyan
	 *
	 */
	public class UnserializableException extends IllegalArgumentException {
		private static final long serialVersionUID = 4275041442173875029L;
		private final List<Object> targets = new ArrayList<Object>();

		public UnserializableException(final Object target) {
			addTarget(target);
		}

		public void addTarget(final Object target) {
			targets.add(targets);
		}

		@Override
		public String getMessage() {
			return "Object not serializable [" + targets + "]";
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
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private Object read(final DataInputStream in) throws IllegalArgumentException, IllegalAccessException, IOException,
			InstantiationException, ClassNotFoundException, SecurityException, InvocationTargetException {
		final byte type;
		try {
			type = in.readByte();
			return read(in, type);
		} catch (final EOFException e) {
			return null;
		}
	}

	/**
	 * reads an concrete type
	 *
	 * @param in
	 * @param type
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private Object read(final DataInputStream in, final byte type)
			throws IllegalArgumentException, IllegalAccessException, IOException, InstantiationException,
			ClassNotFoundException, SecurityException, InvocationTargetException {
		switch (type) {
		case nullEid:
			return null;
		case boolTrueEid:
		case pBoolTrueEid:
			return Boolean.TRUE;
		case boolFalseEid:
		case pBoolFalseEid:
			return Boolean.FALSE;
		case byteEid:
		case pByteEid:
			return Byte.valueOf(in.readByte());
		case shortEid:
		case pShortEid:
			return Short.valueOf(in.readShort());
		case intEid:
		case pIntEid:
			return Integer.valueOf(in.readInt());
		case longEid:
		case pLongEid:
			return Long.valueOf(in.readLong());
		case floatEid:
		case pFloatEid:
			return Float.valueOf(in.readFloat());
		case doubleEid:
		case pDoubleEid:
			return Double.valueOf(in.readDouble());
		case stringEid:
			return in.readUTF();
		case charEid:
		case pCharEid:
			return Character.valueOf(in.readUTF().charAt(0)); // char
		case objEid:
			return new Object();
		case arrayEid:
			final byte atype = in.readByte();
			final Class<?> comp = idxToClasses.get(atype);
			if (comp == null) {
				LOG.error("Error array component class id not in Serialization context");
				return null;
			}
			return readArray(in, atype, comp);
		case -1:
			throw new IllegalStateException("Unknown Error on serializing object");
		default:
			final Class<?> cl = idxToClasses.get(type);
			if (cl == null) {
				LOG.error("Error class id not in Serialization context");
				return null;
			} else if (cl.isEnum())
				return cl.getEnumConstants()[in.readByte()];
			else {
				// recursive object
				Constructor<?> con;
				try {
					con = cl.getDeclaredConstructor();
				} catch (final NoSuchMethodException e) {
					throw new IllegalStateException("Cant find constructor for class [" + cl.getTypeName() + "]");
				}
				if (!Modifier.isPublic(con.getModifiers()))
					con.setAccessible(true);
				final Object target = con.newInstance();
				final List<Field> fields = getFields(cl);
				for (final Field field : fields)
					if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
						if (!Modifier.isPublic(field.getModifiers()))
							field.setAccessible(true);
						try {
							field.set(target, read(in));
						} catch (final IllegalStateException ex) {
							LOG.error("Error read deffect field [" + field + "][" + ex.getMessage() + "]");
						}
					}
				return filter(target);
			}
		}
	}

	/**
	 * to support class filter overloading
	 *
	 * @param target
	 * @return
	 */
	protected Object filter(final Object target) {
		return target;
	}

	/**
	 * possibly reads arrays recursively
	 *
	 * @param in
	 * @param atype
	 * @param comp
	 * @return
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 */
	private Object readArray(final DataInputStream in, final byte atype, final Class<?> comp) throws IOException,
			IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException {
		final boolean same = in.readBoolean();
		final int alength = in.readInt();
		if (alength == 0)
			return Array.newInstance(comp, alength);

		final Object[] array = new Object[alength];
		for (int ac = 0; ac < alength; ac++)
			if (same) {
				if (comp.isArray()) {
					final Class<?> compClassO = comp.getComponentType();
					final byte aeidO = getClassEid(comp);
					array[ac] = readArray(in, aeidO, compClassO);
				} else
					array[ac] = read(in, atype);
			} else
				array[ac] = read(in);

		final Object res = Array.newInstance(comp, alength);
		if (comp.isPrimitive())
			for (int i = 0; i < alength; i++)
				Array.set(res, i, array[i]);
		else
			System.arraycopy(array, 0, res, 0, alength);
		return res;
	}

	/**
	 * gets all declared fields of an class and its super
	 *
	 * @param cl
	 * @return
	 */
	private static List<Field> getFields(final Class<?> cl) {
		final List<Field> fields = new ArrayList<Field>();
		Class<?> pcl = cl;
		while (pcl != null) {
			fields.addAll(Arrays.asList(pcl.getDeclaredFields()));
			pcl = pcl.getSuperclass(); // next
		}
		return fields;
	}

	/**
	 * returns numerical index for context class
	 *
	 * @param clazz
	 * @param target
	 * @return
	 */
	private byte getEid(final Class<?> clazz, final Object target) {
		if (clazz == null)
			return nullEid;
		else if (clazz == Boolean.class && (Boolean) target)
			return boolTrueEid;
		else if (clazz == Boolean.class && !((Boolean) target))
			return boolFalseEid;
		else if (clazz.isArray())
			return arrayEid;
		return getClassEid(clazz);
	}
}