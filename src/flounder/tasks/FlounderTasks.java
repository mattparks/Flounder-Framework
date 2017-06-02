package flounder.tasks;

import flounder.framework.*;

import java.util.*;

/**
 * A module used for managing tasks on framework updates.
 */
public class FlounderTasks extends Module {
	private List<ITask> tasks;

	/**
	 * Creates a new event manager.
	 */
	public FlounderTasks() {
		super();
	}

	@Handler.Function(Handler.FLAG_INIT)
	public void init() {
		this.tasks = new ArrayList<>();
	}

	@Handler.Function(Handler.FLAG_UPDATE_PRE)
	public void update() {
		new ArrayList<>(tasks).forEach((task) -> {
			task.execute();
			tasks.remove(task);
		}); // TODO: Optimise.
	}

	/**
	 * Adds an task to the que.
	 *
	 * @param task The task to add.
	 */
	public void addTask(ITask task) {
		this.tasks.add(task);
	}

	/**
	 * Removes a task from the que.
	 *
	 * @param task The task to remove.
	 */
	public void removeTask(ITask task) {
		this.tasks.remove(task);
	}

	@Handler.Function(Handler.FLAG_DISPOSE)
	public void dispose() {
		tasks.clear();
	}

	@Instance
	public static FlounderTasks get() {
		return (FlounderTasks) Framework.getInstance(FlounderTasks.class);
	}

	@TabName
	public static String getTab() {
		return "Tasks";
	}
}
