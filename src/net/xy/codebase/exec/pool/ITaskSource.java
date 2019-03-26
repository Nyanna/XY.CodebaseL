package net.xy.codebase.exec.pool;

import net.xy.codebase.concurrent.Semaphore;

public interface ITaskSource {
	public Semaphore getCondition();

	public boolean next(final Worker worker);
}
