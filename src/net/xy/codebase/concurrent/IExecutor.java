package net.xy.codebase.concurrent;

public interface IExecutor {

	public int getThreadCount();

	public void shutdown();

	/**
	 * removes self timers from timeout queue
	 */
	public void prepareSutdown();

}