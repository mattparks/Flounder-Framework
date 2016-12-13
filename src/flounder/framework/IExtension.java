package flounder.framework;

import java.util.*;

/**
 * A simple interface that is used to define an extension to the framework. Extensions are used by modules, Example: to use FlounderCamera you must create an extension that implements ICamera.
 */
public abstract class IExtension<T extends IModule> {
	protected static boolean CHANGED_INIT_STATE = true;

	private final List<Class<T>> requires;
	private boolean initialized;

	/**
	 * Creates a new abstract extension.
	 *
	 * @param requires Modules the extension depends on.
	 */
	public IExtension(Class<T>... requires) {
		this.requires = new ArrayList<>(Arrays.asList(requires));
		this.initialized = false;
		FlounderModules.registerModules(FlounderModules.loadModules(requires));
		CHANGED_INIT_STATE = true;
	}

	/**
	 * Gets if the extension is currently active, could be replaced if false.
	 *
	 * @return If the extension is currently active.
	 */
	public abstract boolean isActive();

	/**
	 * Gets the classes that the extension requires.
	 *
	 * @return The classes that the extension requires.
	 */
	protected List<Class<T>> getRequires() {
		return requires;
	}

	/**
	 * Adds a new requirement to the extension.
	 *
	 * @param require The requirement extension to add.
	 */
	protected void addRequirement(Class<T> require) {
		if (!requires.contains(require)) {
			requires.add(require);
		} else {
			return;
		}

		// TODO: Rebuild FlounderModules requirements tree.
	}

	/**
	 * Removes requirement from the extension.
	 *
	 * @param require The requirement extension to remove.
	 */
	protected void removeRequirement(Class<T> require) {
		if (requires.contains(require)) {
			requires.remove(require);
		} else {
			return;
		}

		// TODO: Rebuild FlounderModules requirements tree.
	}

	/**
	 * Gets if the module is initialized.
	 *
	 * @return If the module is initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Sets if the module is initialized.
	 *
	 * @param initialized If the module is initialized.
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
		CHANGED_INIT_STATE = true;
	}
}
