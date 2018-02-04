package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;

public interface ITaskMonitor {

	boolean aquiere();

	void finished();

	IPerfCounter getPerf();

	/**
	 * actuall running threads with tasks from this stripe
	 *
	 * @return
	 */
	int getCurrent();

	/**
	 * last check of the stripe in ts
	 * 
	 * @return
	 */
	long getLastChecked();

}