package flounder.processing;

import flounder.framework.*;
import flounder.logger.*;
import flounder.processing.opengl.*;
import flounder.processing.resource.*;

import java.util.*;

/**
 * A module used for processing types of requests.
 */
public class FlounderProcessors extends Module {
	private List<Processor> processors;

	/**
	 * Creates a new request processor.
	 */
	public FlounderProcessors() {
		super(FlounderLogger.class);
	}

	@Handler.Function(Handler.FLAG_INIT)
	public void init() {
		this.processors = new ArrayList<>();

		// Manually adds the two base processors, these will be added into the modules loop, but are needed now.
		// If these are not added in the init loop, nothing will be able to be initially processed!
		processors.add(new ProcessorResource());
		processors.add(new ProcessorOpenGL());

		// Initializes the processors now.
		processors.forEach(processor -> {
			processor.init();
			processor.setInitialized(true);
		});
	}

	@Handler.Function(Handler.FLAG_UPDATE_PRE)
	public void update() {
		// Gets new processors, if available.
		List<Extension> newProcessors = getExtensionMatches(processors, Processor.class, true);
		cancelChange();

		if (newProcessors != null) {
			List<Processor> newCasted = new ArrayList<>();
			newProcessors.forEach(extension -> newCasted.add(((Processor) extension)));

			// Adds the new processors to the loop.
			if (processors != null) {
				List<Processor> removedStandards = new ArrayList<>();
				removedStandards.addAll(processors);
				removedStandards.removeAll(newCasted);

				// Disposes of any not used processors.
				removedStandards.forEach(removed -> {
					removed.dispose();
					removed.setInitialized(false);
				});
			} else {
				processors = new ArrayList<>();
			}

			processors.clear();
			processors.addAll(newCasted);

			// Initializes any not initialized processors.
			processors.forEach(standard -> {
				if (!standard.isInitialized()) {
					standard.init();
					standard.setInitialized(true);
				}
			});
		}

		// Runs updates for the processors.
		if (processors != null && !processors.isEmpty()) {
			processors.forEach(Processor::update);
		}
	}

	/**
	 * Sends a new resource request to be added to a que.
	 *
	 * @param request The resource request to add.
	 */
	public void sendRequest(Object request) {
		try {
			processors.forEach(processor -> {
				if (processor.getRequestClass().isInstance(request)) {
					processor.addRequestToQueue(request);
				}
			});
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}
	}


	@Handler.Function(Handler.FLAG_DISPOSE)
	public void dispose() {
		// Disposes the processors with the module.
		if (processors != null && !processors.isEmpty()) {
			processors.forEach(processor -> {
				processor.dispose();
				processor.setInitialized(false);
			});
		}
	}

	@Module.Instance
	public static FlounderProcessors get() {
		return (FlounderProcessors) Framework.get().getInstance(FlounderProcessors.class);
	}
}
