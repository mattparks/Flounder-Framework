package flounder.profiling;

import flounder.logger.*;

/**
 * Can be used to record various timings within the framework, timings can be found calling {@link ProfileTimer#getFinalTime()}.
 */
public class ProfileTimer {
	private int invocations;
	private double totalTime;
	private double startTime;
	private double finalTime;

	/**
	 * Creates a new profiling timer.
	 */
	public ProfileTimer() {
		invocations = 0;
		totalTime = 0.0;
		startTime = 0.0;
	}

	/**
	 * Starts a new invocation.
	 */
	public void startInvocation() {
		startTime = System.nanoTime();
	}

	/**
	 * Stops the current Invocation.
	 */
	public void stopInvocation() {
		if (startTime == 0) {
			FlounderLogger.error("Stop Invocation called without matching start invocation!");
			assert (startTime != 0); // Stops from running faulty data.
		}

		invocations++;
		totalTime += System.nanoTime() - startTime;
		startTime = 0;
	}

	/**
	 * Gets the total time taken in seconds.
	 *
	 * @return Returns the total time taken in seconds.
	 */
	public double getFinalTime() {
		return finalTime;
	}

	/**
	 * Calculates the total time taken in seconds, and resets the timer.
	 */
	public void reset() {
		finalTime = (float) ((totalTime * 1e-9) / ((float) invocations));
		invocations = 0;
		totalTime = 0;
		startTime = 0;
	}
}
