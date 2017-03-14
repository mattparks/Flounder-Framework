package flounder.framework.updater;

/**
 * A class used to define how the framework will run updates and timings.
 */
public interface IUpdater {
	/**
	 * Function used to initialize the framework.
	 */
	void initialize();

	/**
	 * Function used to update the framework.
	 */
	void update();

	/**
	 * Function used to profile the framework.
	 */
	void profile();

	/**
	 * Disposed the framework if initialised.
	 */
	void dispose();

	/**
	 * Gets the added/removed time for the framework (seconds).
	 *
	 * @return The time offset.
	 */
	float getTimeOffset();

	/**
	 * Sets the time offset for the framework (seconds).
	 *
	 * @param timeOffset The new time offset.
	 */
	void setTimeOffset(float timeOffset);

	/**
	 * Gets the delta (seconds) between updates.
	 *
	 * @return The delta between updates.
	 */
	float getDelta();

	/**
	 * Gets the delta (seconds) between renders.
	 *
	 * @return The delta between renders.
	 */
	float getDeltaRender();

	void setFpsLimit(float fpsLimit);

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in seconds.
	 */
	float getTimeSec();

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in milliseconds.
	 */
	float getTimeMs();
}
