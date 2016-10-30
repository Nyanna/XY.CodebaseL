package net.xy.codebase.reflec;

import java.lang.reflect.Field;

public class ReflectionUtils {

	public static InstanceField getField(final String fqn, final Object obj) {
		final String[] fields = fqn.split("\\.");
		Class<?> curClass = obj instanceof Class ? (Class<?>) obj : obj.getClass();
		Object curObject = obj instanceof Class ? null : obj;
		Field curField = null;
		Object lastObject;

		for (final String field : fields)
			try {
				for (int i = 0; i < 10; i++)
					try {
						curField = curClass.getDeclaredField(field);
						break;
					} catch (final NoSuchFieldException ex) {
						curClass = curClass.getSuperclass();
						if (curClass == null)
							throw ex;
					}

				curField.setAccessible(true);
				lastObject = curObject;

				curObject = curField.get(lastObject);
				if (curObject != null)
					curClass = curObject.getClass();
			} catch (final NoSuchFieldException e) {
				throw new IllegalArgumentException("Field don't exists [" + field + "][" + fqn + "]");
			} catch (final IllegalArgumentException e) {
				throw e;
			} catch (final IllegalAccessException e) {
				throw new IllegalArgumentException("Field is not accessible [" + field + "][" + fqn + "]", e);
			}
		return new InstanceField(curField, curObject);
	}

	public static class InstanceField {
		public final Field field;
		public final Object object;

		public InstanceField(final Field field, final Object object) {
			this.field = field;
			this.object = object;
		}

		public void set(final Object value) {
			try {
				field.set(object, value);
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException("Can't set value", e);
			} catch (final IllegalAccessException e) {
				throw new IllegalArgumentException("Can't set value", e);
			}
		}

		public Object get() {
			try {
				return field.get(object);
			} catch (final IllegalArgumentException e) {
				throw new IllegalArgumentException("Can't get value", e);
			} catch (final IllegalAccessException e) {
				throw new IllegalArgumentException("Can't get value", e);
			}
		}
	}

	public static boolean isClassLoaded(final String className) {
		try {
			return Class.forName(className) != null;
		} catch (final ClassNotFoundException e) {
		}
		return false;
	}
}
