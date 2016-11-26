package flounder.processing.resource;

import flounder.framework.*;
import flounder.logger.*;
import flounder.processing.*;
import flounder.profiling.*;

/**
 * A extension that is responsible for processing resource requests in a separate thread.
 */
public class ProcessorResource extends IExtension implements IProcessor {
	private Queue<RequestResource> requestQueue;
	private int history;

	private boolean running;
	private Thread thread;

	/**
	 * Creates a new resource processor.
	 */
	public ProcessorResource() {
	}

	@Override
	public void init() {
		this.requestQueue = new Queue<>();
		this.history = 0;

		this.running = true;

		this.thread = new Thread(this::run);
		thread.start();
	}

	@Override
	public void update() {
	}

	@Override
	public void profile() {
		FlounderProfiler.add("Processor", "Requests", requestQueue.count());
		FlounderProfiler.add("Processor", "History", history);
	}

	@Override
	public void addRequestToQueue(Object request) {
		if (!(request instanceof RequestResource)) {
			return;
		}

		boolean isPaused = !requestQueue.hasRequests();
		requestQueue.addRequest((RequestResource) request);

		if (isPaused) {
			indicateNewRequests();
		}

		history++;
	}

	@Override
	public Class getRequestClass() {
		return RequestResource.class;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	private synchronized void run() {
		while (running || requestQueue.hasRequests()) {
			if (requestQueue.hasRequests()) {
				requestQueue.acceptNextRequest().executeRequestResource();
			} else {
				try {
					wait();
				} catch (InterruptedException e) {
					FlounderLogger.log("Request was interrupted.");
					FlounderLogger.exception(e);
				}
			}
		}
	}

	private synchronized void indicateNewRequests() {
		notify();
	}

	@Override
	public void dispose() {
		running = false;
		indicateNewRequests();
		requestQueue.clear();
		history = 0;
	}
}
