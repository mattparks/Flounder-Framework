package flounder.events;

import flounder.framework.*;
import flounder.profiling.*;

import java.util.*;

/**
 * A module used for managing events on framework updates.
 */
public class FlounderEvents extends Module {
	private static final FlounderEvents INSTANCE = new FlounderEvents();
	public static final String PROFILE_TAB_NAME = "Events";

	private List<IEvent> events;

	/**
	 * Creates a new event manager.
	 */
	public FlounderEvents() {
		super(ModuleUpdate.UPDATE_PRE, PROFILE_TAB_NAME);
	}

	@Override
	public void init() {
		this.events = new ArrayList<>();
	}

	@Override
	public void update() {
		events.forEach(event -> {
			if (event.eventTriggered()) {
				event.onEvent();
			}
		});
	}

	/**
	 * Adds an event to the listening que.
	 *
	 * @param event The event to add.
	 */
	public static void addEvent(IEvent event) {
		INSTANCE.events.add(event);
	}

	/**
	 * Removes a event to the listening que.
	 *
	 * @param event The event to remove.
	 */
	public static void removeEvent(IEvent event) {
		INSTANCE.events.remove(event);
	}

	@Override
	public void profile() {
		FlounderProfiler.add(PROFILE_TAB_NAME, "Events Active", events.size());
	}

	@Override
	public Module getInstance() {
		return INSTANCE;
	}

	@Override
	public void dispose() {
		events.clear();
	}
}
