package flounder.events;

import flounder.framework.*;
import flounder.profiling.*;

import java.util.*;

/**
 * A module used for managing events on framework updates.
 */
public class FlounderEvents extends Module {
	private List<IEvent> events;

	/**
	 * Creates a new event manager.
	 */
	public FlounderEvents() {
		super();
	}

	@Handler.Function(Handler.FLAG_INIT)
	public void init() {
		this.events = new ArrayList<>();
	}

	@Handler.Function(Handler.FLAG_UPDATE_PRE)
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
	public void addEvent(IEvent event) {
		this.events.add(event);
	}

	/**
	 * Removes a event to the listening que.
	 *
	 * @param event The event to remove.
	 */
	public void removeEvent(IEvent event) {
		this.events.remove(event);
	}

	@Handler.Function(Handler.FLAG_PROFILE)
	public void profile() {
		FlounderProfiler.get().add(getTab(), "Events Active", events.size());
	}


	@Handler.Function(Handler.FLAG_DISPOSE)
	public void dispose() {
		events.clear();
	}

	@Module.Instance
	public static FlounderEvents get() {
		return (FlounderEvents) Framework.getInstance(FlounderEvents.class);
	}

	@Module.TabName
	public static String getTab() {
		return "Events";
	}
}
