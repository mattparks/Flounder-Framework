package flounder.processing;

/**
 * Interface for defining processors.
 */
public interface IProcessor {
	/**
	 * Run when initializing the processor.
	 */
	void init();

	/**
	 * Run when updating the processor.
	 */
	void update();

	/**
	 * Run when profiling the processor.
	 */
	void profile();

	/**
	 * Used to add a request into the processor.
	 *
	 * @param request The request object to add to the que.
	 */
	void addRequestToQueue(Object request);

	/**
	 * Gets the class used for requests.
	 *
	 * @return The request class used.
	 */
	Class getRequestClass();

	/**
	 * Run when disposing the processor.
	 */
	void dispose();
}
