package net.xy.codebase.concurrent;

public interface IExecutor {

	public int getThreadCount();

	public int getWorkCount();

	public int getIdleCount();

	public void check();

	public void setCoreAmount(int coreAmount);

	public void setMaxAmount(int maxAmount);

	public void shutdown();

	/**
	 * removes self timers from timeout queue
	 */
	public void prepareSutdown();

}