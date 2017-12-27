package net.xy.codebase.concurrent;

public interface IExecutor<E> {

	int getThreadCount();

	int getWorkCount();

	int getIdleCount();

	void check();

	void setCoreAmount(int coreAmount);

	void setMaxAmount(int maxAmount);

	void shutdown();

}