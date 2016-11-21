package net.xy.codebase.reflec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;

import net.xy.codebase.io.ByteArrayOutputStream;

public class MemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	// [package] > [className] > [Class]
	private final Map<String, Map<String, FileObject>> classDataCache = new HashMap<String, Map<String, FileObject>>();
	private final CustomClassLoader classloader;

	public MemoryFileManager(final CustomClassLoader classloader, final StandardJavaFileManager fm) {
		super(fm);
		this.classloader = classloader;
	}

	public JavaFileObject getJavaFileForOutput(final Location loc, final String binName, final Kind kind,
			final FileObject file) throws IOException {
		if (!Kind.CLASS.equals(kind))
			return super.getJavaFileForOutput(loc, binName, kind, file);

		return new SimpleJavaFileObject(URI.create(binName.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS) {
			@Override
			public OutputStream openOutputStream() throws IOException {
				return new ByteArrayOutputStream() {
					@Override
					public void close() throws IOException {
						insertClass(binName, getArray());
						super.close();
					}
				};
			}
		};
	}

	private synchronized void insertClass(final String binName, final byte[] data) {
		final int idx = binName.lastIndexOf('.');
		final String packageName = binName.substring(0, idx);
		final String className = binName.substring(idx + 1);

		Map<String, FileObject> packagemap = classDataCache.get(packageName);
		if (packagemap == null)
			classDataCache.put(packageName, packagemap = new HashMap<String, FileObject>());
		classloader.addClass(binName, data);
		packagemap.put(className, new FileObject(binName, data));
	}

	@Override
	public Iterable<JavaFileObject> list(final Location loc, final String packageName, final Set<Kind> kinds,
			final boolean subpackages) throws IOException {
		if (!kinds.contains(Kind.CLASS))
			return super.list(loc, packageName, kinds, subpackages);

		final List<JavaFileObject> res = new ArrayList<JavaFileObject>();
		for (final JavaFileObject entry : super.list(loc, packageName, kinds, subpackages))
			res.add(entry);

		final Map<String, FileObject> pkg = classDataCache.get(packageName);
		if (pkg != null)
			res.addAll(pkg.values());
		return res;
	}

	@Override
	public String inferBinaryName(final Location loc, final JavaFileObject file) {
		if (file instanceof FileObject)
			return ((FileObject) file).getClassName();
		else
			return super.inferBinaryName(loc, file);
	}

	private class FileObject extends SimpleJavaFileObject {
		private final String fqnName;
		private final byte[] data;

		public FileObject(final String fqnName, final byte[] data) {
			super(URI.create(fqnName + Kind.CLASS.extension), Kind.CLASS);
			this.fqnName = fqnName;
			this.data = data;
		}

		public String getClassName() {
			return fqnName;
		}

		@Override
		public InputStream openInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}
	}
}