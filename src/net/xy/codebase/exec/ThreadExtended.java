package net.xy.codebase.exec;

public class ThreadExtended extends Thread implements IThreadExtended {
	private final String name;

	public ThreadExtended(final String name, final boolean deamon) {
		this.name = name;
		setName(name);
		setDaemon(deamon);
	}

	@Override
	public String getThreadName() {
		return name;
	}
}
