package net.xy.codebase.concurrent;

import net.xy.codebase.exec.IPerfCounter;

public interface ITaskMonitor {

	boolean aquiere();

	void finished();

	IPerfCounter getPerf();

}