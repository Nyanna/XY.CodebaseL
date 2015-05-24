package net.xy.codebase.thread;

/**
 * performance counter for measuring lopp performances
 *
 * @author Xyan
 *
 */
public interface IPerfCounter {
	/**
	 * @return the last loop time
	 */
	public long getLastLoopTime();

	/**
	 * @return the average loop time
	 */
	public long getAvrLoopTime();

	/**
	 * start measuring an loop
	 */
	public void startLoop();

	/**
	 * resume measuring from pause for in loop pauses
	 */
	public void startMeasure();

	/**
	 * currently pauses the measure
	 */
	public void stopMeasure();

	/**
	 * end the loop measure and calculate the average
	 */
	public void endLoop();
}