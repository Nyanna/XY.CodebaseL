package net.xy.codebase.jmx;

public interface IJMXRegistry {

	public void register(String name, String component, Object inst);

	public void register(String name, Object inst);

	public void register(Object inst);

	public void unregister(String name, String component, Class<?> clazz);

	public void unregister(String name, Class<?> clazz);

	public void unregister(Class<?> clazz);
}
