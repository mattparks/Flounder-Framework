package flounder.events;

import flounder.framework.*;

import java.util.*;

/**
 * A module used for managing events on framework updates.
 */
public class FlounderEvents extends IModule {
	private static final FlounderEvents instance = new FlounderEvents();

	private List<IEvent> events;

	/**
	 * Creates a new event manager.
	 */
	public FlounderEvents() {
		super(ModuleUpdate.UPDATE_PRE);
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
		instance.events.add(event);
	}

	/**
	 * Removes a event to the listening que.
	 *
	 * @param event The event to remove.
	 */
	public static void removeEvent(IEvent event) {
		instance.events.remove(event);
	}

	@Override
	public void profile() {
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		events.clear();
	}
}
