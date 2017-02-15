package flounder.framework;

import flounder.helpers.*;

import java.util.*;

/**
 * A simple interface that is used to define an extension to the framework. Extensions are used by modules, Example: to use FlounderCamera you must create an extension that implements ICamera.
 */
public abstract class Extension<T extends Module> {
	private final Module extendedModule;
	private Class<T>[] requires;
	private boolean initialized;

	/**
	 * Creates a new abstract extension.
	 *
	 * @param extendedModule The {@link Module} the extension extends.
	 * @param requires Modules the extension depends on.
	 */
	public Extension(Class<T> extendedModule, Class<T>... requires) {
		this.extendedModule = Framework.loadModule(extendedModule);
		this.requires = ArrayUtils.addElement(requires, extendedModule);
		this.initialized = false;

		if (Framework.getInstance() != null) {
			this.extendedModule.registerExtension(this);
		}
	}

	/**
	 * Gets if the extension is currently active, could be replaced if false.
	 *
	 * @return If the extension is currently active.
	 */
	public abstract boolean isActive();

	/**
	 * Gets the module that the extension extends.
	 *
	 * @return The module that the extension extends.
	 */
	public Module getExtendedModule() {
		return extendedModule;
	}

	/**
	 * Gets the classes that the extension requires.
	 *
	 * @return The classes that the extension requires.
	 */
	protected Class<T>[] getRequires() {
		return requires;
	}

	/**
	 * Adds new requirements to the extension.
	 *
	 * @param toAdd The requirements for the extension to add.
	 */
	protected void addRequirements(Class<T>... toAdd) {
		for (Class<T> require : toAdd) {
			if (!Arrays.asList(requires).contains(require)) {
				this.requires = ArrayUtils.addElement(requires, require);
			} else {
				return;
			}
		}

		Framework.registerModules(Framework.loadModules(requires));
		// TODO: Rebuild FlounderModules requirements tree.
	}

	/**
	 * Removes requirements from the extension.
	 *
	 * @param toRemove The requirements for the extension to remove.
	 */
	protected void removeRequirements(Class<T>... toRemove) {
		for (Class<T> require : toRemove) {
			if (Arrays.asList(requires).contains(require)) {
				this.requires = ArrayUtils.removeElement(requires, require);
			} else {
				return;
			}
		}

		Framework.registerModules(Framework.loadModules(requires));
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
	}
}
