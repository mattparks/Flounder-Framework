package flounder.processing.resource;

import flounder.logger.*;
import flounder.processing.*;
import flounder.profiling.*;

/**
 * A extension that is responsible for processing resource requests in a separate thread.
 */
public class ProcessorResource extends Processor {
	private Queue<RequestResource> requestQueue;
	private int history;

	private boolean running;
	private Thread thread;

	/**
	 * Creates a new resource processor.
	 */
	public ProcessorResource() {
		super();
	}

	@Override
	public void init() {
		this.requestQueue = new Queue<>();
		this.history = 0;

		this.running = true;

		this.thread = new Thread(this::run);
		thread.setName("resources");
		thread.start();
	}

	@Override
	public void update() {
	}

	@Override
	public void profile() {
		FlounderProfiler.get().add(FlounderProcessors.getTab(), "Resource Requests", requestQueue.count());
		FlounderProfiler.get().add(FlounderProcessors.getTab(), "Resource History", history);
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

	private synchronized void run() {
		while (running || requestQueue.hasRequests()) {
			if (requestQueue.hasRequests()) {
				requestQueue.acceptNextRequest().executeRequestResource();
			} else {
				try {
					wait();
				} catch (InterruptedException e) {
					FlounderLogger.get().log("Request was interrupted.");
					FlounderLogger.get().exception(e);
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

	@Override
	public boolean isActive() {
		return true;
	}
}
