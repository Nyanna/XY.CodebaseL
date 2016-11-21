package net.xy.codebase.reflec;

import java.util.HashMap;
import java.util.Map;

import javax.tools.StandardJavaFileManager;

public class CustomClassLoader extends ClassLoader {
	private final Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();
	private final MemoryFileManager fileMan;

	public CustomClassLoader(final StandardJavaFileManager stdFileMan) {
		this(Thread.currentThread().getContextClassLoader(), stdFileMan);
	}

	public CustomClassLoader(final ClassLoader parentalCl, final StandardJavaFileManager stdFileMan) {
		super(parentalCl);
		fileMan = new MemoryFileManager(this, stdFileMan);
	}

	public MemoryFileManager getFileManager() {
		return fileMan;
	}

	public boolean isLoaded(final String paramString) {
		return classCache.get(paramString) != null;
	};

	@Override
	protected synchronized Class<?> findClass(final String paramString) throws ClassNotFoundException {
		final Class<?> clazz = classCache.get(paramString);
		if (clazz != null)
			return clazz;
		return super.findClass(paramString);
	};

	public synchronized void addClass(final String fqnName, final byte[] data) {
		if (classCache.containsKey(fqnName))
			throw new IllegalArgumentException("Tried to define class twice [" + fqnName + "]");

		final String packageName = fqnName.substring(0, fqnName.lastIndexOf('.'));
		if (getPackage(packageName) == null)
			definePackage(packageName, null, null, null, null, null, null, null);
		classCache.put(fqnName, defineClass(fqnName, data, 0, data.length));
	}
}
