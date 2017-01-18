package flounder.processing;

import flounder.framework.*;
import flounder.logger.*;
import flounder.processing.opengl.*;
import flounder.processing.resource.*;
import flounder.profiling.*;

import java.util.*;

/**
 * A module used for processing types of requests.
 */
public class FlounderProcessors extends IModule {
	private static final FlounderProcessors INSTANCE = new FlounderProcessors();
	public static final String PROFILE_TAB_NAME = "Processors";

	private List<IProcessor> processors;

	/**
	 * Creates a new request processor.
	 */
	public FlounderProcessors() {
		super(ModuleUpdate.UPDATE_PRE, PROFILE_TAB_NAME, FlounderLogger.class, FlounderProfiler.class);
	}

	@Override
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

	@Override
	public void update() {
		// Gets new processors, if available.
		List<IExtension> newProcessors = getExtensions();

		if (newProcessors != null) {
			List<IProcessor> newCasted = new ArrayList<>();
			newProcessors.forEach(extension -> newCasted.add(((IProcessor) extension)));

			// Adds the new processors to the loop.
			if (processors != null) {
				List<IProcessor> removedStandards = new ArrayList<>();
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
			processors.forEach(IProcessor::update);
		}
	}

	@Override
	public void profile() {
		if (processors != null && !processors.isEmpty()) {
			processors.forEach(IProcessor::profile);
		}

		FlounderProfiler.add(PROFILE_TAB_NAME, "Processors", processors.size());
	}

	/**
	 * Sends a new resource request to be added to a que.
	 *
	 * @param request The resource request to add.
	 */
	public static void sendRequest(Object request) {
		INSTANCE.processors.forEach(processor -> {
			if (processor.getRequestClass().isInstance(request)) {
				processor.addRequestToQueue(request);
			}
		});
	}

	@Override
	public IModule getInstance() {
		return INSTANCE;
	}

	@Override
	public void dispose() {
		// Disposes the processorss with the module.
		if (processors != null && !processors.isEmpty()) {
			processors.forEach(processor -> {
				processor.dispose();
				processor.setInitialized(false);
			});
		}
	}
}
