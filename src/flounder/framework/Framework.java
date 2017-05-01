package flounder.framework;

import flounder.framework.updater.*;
import flounder.logger.*;
import flounder.profiling.*;
import flounder.resources.*;
import flounder.standards.*;

import java.io.*;
import java.util.*;

/**
 * A framework used for simplifying the creation of complicated Java applications. By using flexible Module loading and Extension injecting, it allows the engine to be used for Networking, Imaging, AIs, Games, and many more applications.
 * Start off by creating a new Framework object in your main thread, using Extensions in the constructor. By using Extensions: Modules can be required and therefor loaded into the framework.
 * Implementing interfaces like {@link Standard} with your extension can allow you do task specific things with your Extensions. After creating your Framework object call {@link #run()} to start.
 */
public class Framework {
	private static boolean runningFromJar;
	private static MyFile roamingFolder;

	private static Version version;
	private static IUpdater updater;

	private static List<Module> modules;
	private static List<Module> overrides;
	private static boolean initialized;
	private static boolean running;
	private static int fpsLimit;

	/**
	 * Carries out the setup for basic framework components and the framework. Call {@link #run()} after creating a instance.
	 *
	 * @param unlocalizedName The name to be used when determining where the roaming save files are saved.
	 * @param updater The definition for how the framework will run.
	 * @param fpsLimit The limit to FPS, (-1 disables limits).
	 * @param extensions The extensions to load for the framework.
	 * @param overrides The module overrides to load for the framework.
	 */
	public Framework(String unlocalizedName, IUpdater updater, int fpsLimit, Extension[] extensions, Module[] overrides) {
		// Loads some simple framework runtime info.
		loadFlounderStatics(unlocalizedName);

		// Increment revision every fix for the minor version release. Minor version represents the build month. Major incremented every two years OR after major core framework rewrites.
		Framework.version = new Version("30.04.12");

		// Sets the frameworks updater.
		Framework.updater = updater;
		Framework.updater.setFpsLimit(fpsLimit);

		// Sets up the module and extension managers.
		Framework.modules = new ArrayList<>();
		Framework.overrides = Arrays.asList(overrides);

		// Registers these modules as global, we do this as everyone loves these guys <3
		registerModules(loadModule(FlounderLogger.class));
		registerModules(loadModule(FlounderProfiler.class));

		// Force registers the extensions, as the framework was null when they were constructed.
		for (Extension extension : extensions) {
			registerModule(loadModule(extension.getModule())).registerExtension(extension);
		}

		Framework.initialized = false;
		Framework.running = true;
		Framework.fpsLimit = fpsLimit;
	}

	/**
	 * Called before the framework loads, used to setup roaming folders and other statics.
	 *
	 * @param unlocalizedName The implementations name, used to set the roaming save folder.
	 */
	private static void loadFlounderStatics(String unlocalizedName) {
		runningFromJar = Framework.class.getResource("/" + Framework.class.getName().replace('.', '/') + ".class").toString().startsWith("jar:");
		String saveDir;

		if (System.getProperty("os.name").contains("Windows")) {
			saveDir = System.getenv("APPDATA");
		} else {
			saveDir = System.getProperty("user.home");
		}

		roamingFolder = new MyFile(saveDir, "." + unlocalizedName);
		File save = new File(saveDir + "/." + unlocalizedName + "/");

		if (!save.exists()) {
			System.out.println("Creating directory: " + save);

			try {
				save.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		LoggerFrame logger = null;

		try {
			updater.run();
		} catch (Exception e) {
			FlounderLogger.get().exception(e);
			logger = new LoggerFrame();
		} finally {
			updater.dispose();
		}

		if (logger != null) {
			logger.run();
		}
	}

	/**
	 * Runs the handlers using a specific flag.
	 *
	 * @param flag The flag to run from.
	 */
	public static void runHandlers(int flag) {
		for (Module module : modules) {
			module.getInstance().runHandler(flag);
		}
	}

	/**
	 * Gets if the framework is currently running from a jar.
	 *
	 * @return Is the framework is currently running from a jar?
	 */
	public static boolean isRunningFromJar() {
		return Framework.runningFromJar;
	}

	/**
	 * Gets the file that goes to the roaming folder.
	 *
	 * @return The roaming folder file.
	 */
	public static MyFile getRoamingFolder() {
		return Framework.roamingFolder;
	}

	/**
	 * Gets the frameworks current version.
	 *
	 * @return The frameworks current version.
	 */
	public static Version getVersion() {
		return Framework.version;
	}

	/**
	 * Gets the frameworks updater.
	 *
	 * @return The updater.
	 */
	public static IUpdater getUpdater() {
		return Framework.updater;
	}

	public static List<Module> getModules() {
		return modules;
	}

	public static List<Module> getOverrides() {
		return overrides;
	}

	/**
	 * Gets a loaded and registered module from the framework.
	 *
	 * @param object The module class.
	 *
	 * @return The module.
	 */
	public static Module getModule(Class object) {
		if (containsModule(object)) {
			for (Module module : modules) {
				if (object.isInstance(module)) {
					return module;
				}
			}
		}

		return null;
	}

	/**
	 * Gets a loaded and registered module override from the framework.
	 *
	 * @param parent The module parent class.
	 *
	 * @return The module override.
	 */
	public static Module getOverride(Class parent) {
		for (Module module : overrides) {
			if (!module.getClass().equals(parent) && parent.isInstance(module)) {
				return module;
			}
		}

		return null;
	}

	/**
	 * Gets a module instance, or the override to the module.
	 *
	 * @param object The module class.
	 *
	 * @return The module instance.
	 */
	public static Module getInstance(Class object) {
		Module override = Framework.getOverride(object);
		Module actual = Framework.getModule(object);
		return override == null ? actual : override;
	}

	/**
	 * Gets if the framework contains a module.
	 *
	 * @param object The module class.
	 *
	 * @return If the framework contains a module.
	 */
	protected static boolean containsModule(Class object) {
		for (Module m : modules) {
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
	 * Loads a module into a Module class and gets the instance.
	 *
	 * @param object The module class.
	 *
	 * @return The module INSTANCE class.
	 */
	protected static Module loadModule(Class object) {
		Module m = getModule(object);

		if (m != null) {
			return m;
		}

		try {
			return ((Module) object.newInstance()).getInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			System.err.println("Module class path " + object.getName() + " constructor could not be found!");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Loads a list of modules into Module classes and gets all of the instances.
	 *
	 * @param objects The module classes.
	 *
	 * @return The modules instance classes.
	 */
	protected static Module[] loadModules(Class... objects) {
		Module[] result = new Module[objects.length];

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
	protected static Module registerModule(Module module) {
		if (module == null || containsModule(module.getClass())) {
			return module;
		}

		// Add the module temporally.
		modules.add(module);

		// Will load and register required modules if needed.
		if (!containsModules(module.getDependencies())) {
			// Registers all required modules.
			registerModules(loadModules(module.getDependencies()));

			// Add the module to the modules list.
			modules.remove(module);
			modules.add(module);
		}

		// Initialize modules if needed,
		if (initialized && module.hasHandlerRun(Handler.FLAG_INIT)) {
			module.getHandler(Handler.FLAG_INIT).run();
		}

		return module;
	}

	/**
	 * Registers a list of modules, and initializes them if the engine has already started.
	 *
	 * @param list The list of modules to register.
	 */
	protected static void registerModules(Module... list) {
		for (Module module : list) {
			registerModule(module);
		}
	}

	/**
	 * Logs all information from a module.
	 */
	public static void logModules() {
		// Logs all registered modules.
		for (Module module : modules) {
			// Log module data.
			String requires = "";
			String override = "" + getOverride(module.getClass());

<<<<<<< HEAD
			for (int i = 0; i < module.getDependencies().length; i++) {
				requires += module.getDependencies()[i].getSimpleName() + ((i == module.getDependencies().length - 1) ? "" : ", ");
			}
=======
	public static IUpdater getUpdater() {
		return INSTANCE.updater;
	}

	public static List<Module> getModulesActive() {
		return INSTANCE.modulesActive;
	}
>>>>>>> master

			FlounderLogger.get().register("Registering " + module.getClass().getSimpleName() + ": " + FlounderLogger.ANSI_PURPLE + "Override(" + override + "): " + FlounderLogger.ANSI_RED + "Requires(" + requires + ")" + FlounderLogger.ANSI_RESET);
		}
	}

	/**
	 * Gets the added/removed time for the framework (seconds).
	 *
	 * @return The time offset.
	 */
	public static float getTimeOffset() {
		return Framework.updater.getTimeOffset();
	}

	/**
	 * Sets the time offset for the framework (seconds).
	 *
	 * @param timeOffset The new time offset.
	 */
	public static void setTimeOffset(float timeOffset) {
		Framework.updater.setTimeOffset(timeOffset);
	}

	/**
	 * Gets the delta (seconds) between updates.
	 *
	 * @return The delta between updates.
	 */
	public static float getDelta() {
		return Framework.updater.getDelta();
	}

	/**
	 * Gets the delta (seconds) between renders.
	 *
	 * @return The delta between renders.
	 */
	public static float getDeltaRender() {
		return Framework.updater.getDeltaRender();
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in seconds.
	 */
	public static float getTimeSec() {
		return Framework.updater.getTimeSec();
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in milliseconds.
	 */
	public static float getTimeMs() {
		return Framework.updater.getTimeMs();
	}

	/**
	 * Gets if the framework is currently initialized.
	 *
	 * @return Is the framework is currently initialized?
	 */
	public static boolean isInitialized() {
		return Framework.initialized;
	}

	/**
	 * Sets if the framework is initialized.
	 *
	 * @param initialized If the framework is initialized.
	 */
	public static void setInitialized(boolean initialized) {
		Framework.initialized = initialized;
	}

	/**
	 * Gets if the framework still running.
	 *
	 * @return Is the framework still running?
	 */
	public static boolean isRunning() {
		return Framework.running;
	}

	/**
	 * Requests the implementation-loop to stop and the implementation to exit.
	 */
	public static void requestClose() {
		Framework.running = false;
	}

	/**
	 * Gets the current FPS limit.
	 *
	 * @return The current FPS limit.
	 */
	public static int getFpsLimit() {
		return Framework.fpsLimit;
	}

	/**
	 * Sets a limit to the fps, (-1 disabled limits).
	 *
	 * @param fpsLimit The FPS limit.
	 */
	public static void setFpsLimit(int fpsLimit) {
		Framework.fpsLimit = fpsLimit;
		Framework.updater.setFpsLimit(fpsLimit);
	}
}
