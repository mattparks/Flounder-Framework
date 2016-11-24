package flounder.framework;

/**
 * A simple interface that is used to define an extension to the framework. Extensions are used by modules, Example: to use FlounderCamera you must create an extension that implements ICamera.
 */
public abstract class IExtension<T extends IModule> {
	protected static boolean CHANGED_INIT_STATE = true;

	private final Class<T>[] requires;
	private boolean initialized;

	/**
	 * Creates a new abstract extension.
	 *
	 * @param requires Modules the extension depends on.
	 */
	public IExtension(Class<T>... requires) {
		this.requires = requires;
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
	protected Class<T>[] getRequires() {
		return requires;
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
