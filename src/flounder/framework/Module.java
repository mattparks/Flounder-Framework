package flounder.framework;

import flounder.profiling.*;

import java.util.*;

/**
 * A simple interface that can be used to create framework modules.
 */
public abstract class Module<T extends Module> {
	private ModuleUpdate moduleUpdate;
	private String profileTab;
	private final Class<T>[] requires;
	private List<Extension> extensions;
	private ProfileTimer profileTimer;
	private boolean initialized;

	/**
	 * Creates a new abstract module.
	 *
	 * @param moduleUpdate How/when the module will update.
	 * @param requires Classes the module depends on.
	 */
	public Module(ModuleUpdate moduleUpdate, String profileTab, Class<T>... requires) {
		this.moduleUpdate = moduleUpdate;
		this.profileTab = profileTab;
		this.requires = requires;
		this.extensions = new ArrayList<>();
		this.profileTimer = new ProfileTimer();
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
	 * Gets how/when the module will update.
	 *
	 * @return How/when the module will update.
	 */
	public ModuleUpdate getModuleUpdate() {
		return moduleUpdate;
	}

	/**
	 * Gets the name of the profile tab for this module.
	 *
	 * @return The modules profile tab name.
	 */
	public String getProfileTab() {
		return profileTab;
	}

	/**
	 * Gets the classes that the module requires.
	 *
	 * @return The classes that the module requires.
	 */
	protected Class<T>[] getRequires() {
		return requires;
	}

	/**
	 * Registers an extension with a module.
	 *
	 * @param extension The extension to register.
	 */
	protected void registerExtension(Extension extension) {
		if (!extensions.contains(extension)) {
			Framework.registerModules(Framework.loadModules(extension.getRequires()));
			Framework.forceChange();
			extensions.add(extension);
		}
	}

	/**
	 * Registers extensions with a module.
	 *
	 * @param extensions The extensions to register.
	 */
	protected void registerExtensions(Extension... extensions) {
		for (Extension extension : extensions) {
			registerExtension(extension);
		}
	}

	/**
	 * Finds a new extension for this module that implements an interface/class.
	 *
	 * @param last The last object to compare to.
	 * @param type The class type of object to find a extension that matches for.
	 * @param onlyRunOnChange When this and {@link Framework#extensionsChanged} is true, this will update a check, otherwise a object will not be checked for (returning null).
	 * @param <Y> The type of extension class to be found.
	 *
	 * @return The found extension to be active and matched the specs provided.
	 */
	public <Y> Extension getExtensionMatch(Extension last, Class<Y> type, boolean onlyRunOnChange) {
		if (Framework.isChanged() || !onlyRunOnChange) {
			if (!extensions.isEmpty()) {
				for (Extension extension : extensions) {
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
	public List<Extension> getExtensions() {
		return extensions;
	}

	public ProfileTimer getProfileTimer() {
		return profileTimer;
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
	public abstract Module getInstance();

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
