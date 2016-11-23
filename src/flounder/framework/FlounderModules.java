package flounder.framework;

import flounder.logger.*;

import java.util.*;

import static flounder.framework.FlounderFramework.*;

/**
 * A class that manages static modules.
 */
public class FlounderModules {
	protected static final List<IModule> modulesActive = new ArrayList<>();
	protected static final List<String> modulesUnlogged = new ArrayList<>();

	/**
	 * Gets if the framework contains a module.
	 *
	 * @param object The module class.
	 *
	 * @return If the framework contains a module.
	 */
	protected static boolean containsModule(Class object) {
		for (IModule m : modulesActive) {
			if (m.getClass().getName().equals(object.getName())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets if the framework contains all of the modules.
	 *
	 * @param objects The module classes.
	 *
	 * @return If the framework contains all of the modules.
	 */
	protected static boolean containsModules(Class... objects) {
		for (Class object : objects) {
			if (!containsModule(object)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Loads a module into a IModule class and gets the instance.
	 *
	 * @param object The module class.
	 *
	 * @return The module instance class.
	 */
	protected static IModule loadModule(Class object) {
		try {
			return ((IModule) object.newInstance()).getInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			System.err.println("IModule class path " + object.getName() + " constructor could not be found!");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Loads a list of modules into IModule classes and gets all of the instances.
	 *
	 * @param objects The module classes.
	 *
	 * @return The modules instance classes.
	 */
	protected static IModule[] loadModules(Class... objects) {
		IModule[] result = new IModule[objects.length];

		for (int i = 0; i < objects.length; i++) {
			result[i] = loadModule(objects[i]);
		}

		return result;
	}

	/**
	 * Registers a module, and initializes if the engine has already started.
	 *
	 * @param module The module to register.
	 */
	protected static void registerModule(IModule module) {
		if (module == null || containsModule(module.getClass())) {
			return;
		}

		// Add the module temporally.
		modulesActive.add(module);

		// registerModules(loadModules(module.getRequires()));

		for (int i = module.getRequires().length - 1; i >= 0; i--) {
			registerModule(loadModule(module.getRequires()[i]));
		}

		// Add the module after all required.
		modulesActive.remove(module);
		modulesActive.add(module);

		// Initialize modules if needed,
		if (isInitialized() && !module.isInitialized()) {
			module.init();
			module.setInitialized(true);
		}

		// Log module data.
		String requires = "";

		for (int i = 0; i < module.getRequires().length; i++) {
			requires += module.getRequires()[i].getSimpleName() + ((i == module.getRequires().length - 1) ? "" : ", ");
		}

		String logOutput = "Registering " + module.getClass().getSimpleName() + ":" + FlounderLogger.ANSI_PURPLE + " (" + (isInitialized() ? "POST_INIT, " : "") + "UPDATE_" + module.getModuleUpdate().name() + ")" + FlounderLogger.ANSI_RESET + ":" + FlounderLogger.ANSI_RED + " Requires(" + requires + ")" + FlounderLogger.ANSI_RESET;

		if (isInitialized() && containsModule(FlounderLogger.class)) {
			if (!modulesUnlogged.isEmpty()) {
				modulesUnlogged.forEach(FlounderLogger::register);
			}

			modulesUnlogged.clear();
			FlounderLogger.register(logOutput);
		} else {
			modulesUnlogged.add(logOutput);
		}
	}

	/**
	 * Registers a list of modules, and initializes them if the engine has already started.
	 *
	 * @param modules The list of modules to register.
	 */
	protected static void registerModules(IModule... modules) {
		for (IModule module : modules) {
			registerModule(module);
		}
	}
}
