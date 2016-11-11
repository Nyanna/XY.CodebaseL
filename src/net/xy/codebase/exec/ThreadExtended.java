package net.xy.codebase.exec;

public class ThreadExtended extends Thread {
	private final String name;

	public ThreadExtended(final String name, final boolean deamon) {
		this.name = name;
		setName(name);
		setDaemon(deamon);
	}

	public String getThreadName() {
		return name;
	}
}
