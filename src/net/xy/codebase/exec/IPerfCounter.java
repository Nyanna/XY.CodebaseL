package net.xy.codebase.exec;

/**
 * performance counter for measuring lopp performances
 *
 * @author Xyan
 *
 */
public interface IPerfCounter {
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

	/**
	 * @return the last loop time
	 */
	public long getLastLoopTime();

	/**
	 * @return the average loop time
	 */
	public long getAvrLoopTime();

	/**
	 * average time from one call to loop end to the next one, called intervall
	 * time
	 *
	 * @return
	 */
	public long getAvrLoopIntervalTime();

	/**
	 * last time from one call to loop end to the next one, called intervall
	 * time
	 *
	 * @return
	 */
	public long getLastIntervall();

	/**
	 * timestamp of last time data were aggregated
	 *
	 * @return
	 */
	public long lastUpdate();

	/**
	 * age of last updated related to nanoTime
	 * 
	 * @param nanoTime
	 * @return
	 */
	public long lastUpdateAge(long nanoTime);
}