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
	 * start measure with given time
	 *
	 * @param nanoTime
	 */
	public void startMeasure(long nanoTime);

	/**
	 * currently pauses the measure
	 */
	public void stopMeasure();

	/**
	 * stop measure with given time
	 *
	 * @param nanoTime
	 */
	public void stopMeasure(long nanoTime);

	/**
	 * end the loop measure and calculate the average
	 */
	public void endLoop();

	/**
	 * end loop with given time
	 *
	 * @param nanoTime
	 */
	public void endLoop(long nanoTime);

	/**
	 * @return the last loop time
	 */
	public long getLastLoopTime();

	/**
	 * @return the average loop time
	 */
	public long getAvrLoopTime();

	/**
	 * average time from one call to loop end to the next one, called intervall time
	 *
	 * @return
	 */
	public long getAvrLoopIntervalTime();

	/**
	 * amount of measures in last loop
	 *
	 * @return
	 */
	public long getLastLoopCalls();

	/**
	 * last time from one call to loop end to the next one, called intervall time
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

	/**
	 * idletime in intervall
	 *
	 * @return
	 */
	public long getIdleTime();

	/**
	 * fractional amount of idletime per intervall
	 *
	 * @return
	 */
	public float getUseFraction();

	/**
	 * average usage fraction
	 *
	 * @return
	 */
	public float getUseAvrFraction();

	/**
	 * bypass the intern data stores nad just count a measure
	 *
	 * @param nanoTime
	 * @param measureStart
	 */
	public void countMeasure(long nanoTime, long measureStart);
}