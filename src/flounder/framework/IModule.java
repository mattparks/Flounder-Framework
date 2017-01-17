package flounder.framework;

import java.util.*;

/**
 * A simple interface that can be used to create framework modules.
 */
public abstract class IModule<T extends IModule> {
	private final Class<T>[] requires;
	private ModuleUpdate moduleUpdate;
	private List<IExtension> extensions;
	private boolean initialized;

	/**
	 * Creates a new abstract module.
	 *
	 * @param moduleUpdate How/when the module will update.
	 * @param requires Classes the module depends on.
	 */
	public IModule(ModuleUpdate moduleUpdate, Class<T>... requires) {
		this.requires = requires;
		this.moduleUpdate = moduleUpdate;
		this.extensions = new ArrayList<>();
		this.initialized = false;
	}

	/**
	 * Initializes the module.
	 */
	public abstract void init();

	/**
	 * Runs a update of the module.
	 */
	public abstract void update();

	/**
	 * Profiles the module.
	 */
	public abstract void profile();

	/**
	 * Gets the classes that the module requires.
	 *
	 * @return The classes that the module requires.
	 */
	protected Class<T>[] getRequires() {
		return requires;
	}

	/**
	 * Gets how/when the module will update.
	 *
	 * @return How/when the module will update.
	 */
	public ModuleUpdate getModuleUpdate() {
		return moduleUpdate;
	}

	/**
	 * Registers an extension with a module.
	 *
	 * @param extension The extension to register.
	 */
	protected void registerExtension(IExtension extension) {
		if (!extensions.contains(extension)) {
			FlounderFramework.registerModules(FlounderFramework.loadModules(extension.getRequires()));
			FlounderFramework.forceChange();
			extensions.add(extension);
		}
	}

	/**
	 * Registers extensions with a module.
	 *
	 * @param extensions The extensions to register.
	 */
	protected void registerExtensions(IExtension... extensions) {
		for (IExtension extension : extensions) {
			registerExtension(extension);
		}
	}

	/**
	 * Finds a new extension for this module that implements an interface/class.
	 *
	 * @param last The last object to compare to.
	 * @param type The class type of object to find a extension that matches for.
	 * @param onlyRunOnChange When this and {@link flounder.framework.FlounderFramework#extensionsChanged} is true, this will update a check, otherwise a object will not be checked for (returning null).
	 * @param <Y> The type of extension class to be found.
	 *
	 * @return The found extension to be active and matched the specs provided.
	 */
	public <Y> IExtension getExtensionMatch(IExtension last, Class<Y> type, boolean onlyRunOnChange) {
		if (FlounderFramework.isChanged() || !onlyRunOnChange) {
			if (!extensions.isEmpty()) {
				for (IExtension extension : extensions) {
					if (extension.isActive() && type.isInstance(extension) && !extension.equals(last)) {
						return extension;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets all extensions for this module.
	 *
	 * @return This modules extensions.
	 */
	public List<IExtension> getExtensions() {
		return extensions;
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

	/**
	 * Gets the current module instance.
	 *
	 * @return The current module instance.
	 */
	public abstract IModule getInstance();

	/**
	 * Disposes the module.
	 */
	public abstract void dispose();

	/**
	 * A enum that defines where a module will update.
	 */
	public enum ModuleUpdate {
		UPDATE_ALWAYS, UPDATE_PRE, UPDATE_POST, UPDATE_RENDER
	}
}
