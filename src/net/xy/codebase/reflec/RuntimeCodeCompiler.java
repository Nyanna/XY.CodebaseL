package net.xy.codebase.reflec;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * runtime in memory code compiler, will method autodetection
 *
 * @author Xyan
 *
 */
public class RuntimeCodeCompiler {
	private static final Logger LOG = LoggerFactory.getLogger(RuntimeCodeCompiler.class);

	private static final String CLASS_PAT = "(private|protected|public|^)[ ]*(class|interface)[ ]+([a-z0-9]+)";
	private final Pattern classpat = Pattern.compile(CLASS_PAT, Pattern.CASE_INSENSITIVE);
	private final Pattern pckgpat = Pattern.compile("package ([a-z0-9.]+);", Pattern.CASE_INSENSITIVE);
	private final JavaCompiler compiler;
	private final List<String> options = new ArrayList<String>();
	private CustomClassLoader sharedLoader;

	public RuntimeCodeCompiler() {
		compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null)
			throw new IllegalStateException("No compiler found has to be run with an JDK");
		if (compiler.isSupportedOption("Xlint") > -1)
			options.add("Xlint:deprecation");
		resetClassLoader();
	}

	public void resetClassLoader() {
		sharedLoader = new CustomClassLoader(compiler.getStandardFileManager(null, null, null));
	}

	public void execute(final String code, final Object... params) throws Exception {
		final String fqn = compile(code, sharedLoader);
		runClass(sharedLoader.loadClass(fqn), params);
	}

	public synchronized String compile(final String input, final CustomClassLoader cl) {
		final StringWriter sw = new StringWriter();
		final String code, className = getClassname(input);
		if (cl.isLoaded(className))
			code = replaceClassname(className + "_" + System.currentTimeMillis(), input);
		else
			code = input;

		final String packageName = getPackage(code);
		final String packagePath = packageName.length() > 0 ? packageName.replace(".", "/") + "/" : "";
		final List<JavaFileObject> units = new ArrayList<JavaFileObject>();
		units.add(new SimpleJavaFileObject(URI.create(packagePath + className + Kind.SOURCE.extension), Kind.SOURCE) {
			@Override
			public CharSequence getCharContent(final boolean ignoreErrors) {
				return code;
			}
		});
		if (!compiler.getTask(sw, cl.getFileManager(), null, options, null, units).call())
			throw new IllegalStateException("Error compiling unit");
		return packageName.length() > 0 ? packageName + "." + className : className;
	}

	public void runClass(final Class<?> clazz, final Object[] params) throws InstantiationException,
			IllegalAccessException, SecurityException, IllegalArgumentException, InvocationTargetException {
		if (LOG.isDebugEnabled())
			LOG.debug("Start execution of class [" + clazz.getName() + "]");

		if (Runnable.class.isAssignableFrom(clazz))
			((Runnable) clazz.newInstance()).run();
		else {
			final Class<?>[] parac = getParameterClasses(params);
			for (final Method meth : clazz.getMethods())
				if (isCompatible(parac, meth.getParameterTypes()))
					meth.invoke(clazz.newInstance(), params);
		}
	}

	private boolean isCompatible(final Class<?>[] parac, final Class<?>[] types) {
		if ((parac == null || parac.length == 0) && (types == null || types.length == 0))
			return true;
		if (parac == null || types == null || parac.length != types.length)
			return false;
		for (int i = 0; i < parac.length; i++)
			if (!types[i].isAssignableFrom(parac[i]))
				return false;
		return true;
	}

	private Class<?>[] getParameterClasses(final Object[] params) {
		ArrayList<Class<?>> parac = null;
		if (params != null) {
			parac = new ArrayList<Class<?>>();
			for (final Object obj : params)
				parac.add(obj.getClass());
		}
		return parac != null ? parac.toArray(new Class[parac.size()]) : null;
	}

	private String getClassname(final String code) {
		final Matcher m = classpat.matcher(code);
		if (!m.find())
			throw new IllegalArgumentException("Classname not found");
		return m.group(3);
	}

	private String replaceClassname(final String className, final String code) {
		final Matcher m = classpat.matcher(code);
		if (!m.find())
			throw new IllegalArgumentException("Classname not found");
		return code.replace(m.group(), m.group().replace(m.group(3), className));
	}

	private String getPackage(final String code) {
		final Matcher m = pckgpat.matcher(code);
		return m.find() ? m.group(1) : "";
	}
}
