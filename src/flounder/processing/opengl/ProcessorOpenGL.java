package flounder.processing.opengl;

import flounder.framework.*;
import flounder.processing.*;
import flounder.profiling.*;

/**
 * A extension that is responsible for processing OpenGL requests.
 */
public class ProcessorOpenGL extends IExtension implements IProcessor {
	private static final float MAX_TIME_MILLIS = 8.0f;

	private Queue<RequestOpenGL> requestQueue;
	private int history;

	/**
	 * Creates a new OpenGL processor.
	 */
	public ProcessorOpenGL() {
	}

	@Override
	public void init() {
		this.requestQueue = new Queue<>();
		this.history = 0;
	}

	@Override
	public void update() {
		float remainingTime = MAX_TIME_MILLIS * 1000000.0f;
		long start = System.nanoTime();

		while (requestQueue.hasRequests()) {
			requestQueue.acceptNextRequest().executeRequestGL();
			long end = System.nanoTime();
			long timeTaken = end - start;
			remainingTime -= timeTaken;
			start = end;

			if (remainingTime < 0.0f) {
				break;
			}
		}
	}

	@Override
	public void profile() {
		FlounderProfiler.add("GLProcessor", "Requests", requestQueue.count());
		FlounderProfiler.add("GLProcessor", "History", history);
	}

	@Override
	public void addRequestToQueue(Object request) {
		if (!(request instanceof RequestOpenGL)) {
			return;
		}

		requestQueue.addRequest((RequestOpenGL) request);
		history++;
	}

	@Override
	public Class getRequestClass() {
		return RequestOpenGL.class;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	/**
	 * Completes all requests left in queue.
	 */
	public void completeAllRequests() {
		while (requestQueue.hasRequests()) {
			requestQueue.acceptNextRequest().executeRequestGL();
		}
	}

	@Override
	public void dispose() {
		completeAllRequests();
		requestQueue.clear();
		history = 0;
	}
}
