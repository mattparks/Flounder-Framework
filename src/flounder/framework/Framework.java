package flounder.framework;

import flounder.logger.*;
import flounder.maths.*;
import flounder.maths.Timer;
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
public class Framework extends Thread {
	private static Framework INSTANCE;
	public static final String PROFILE_TAB_NAME = "Framework";

	private boolean runningFromJar;
	private MyFile roamingFolder;

	private Version version;

	private boolean closedRequested;
	private long startTime;

	private List<Module> modulesActive;
	private boolean extensionsChanged;

	private Delta deltaUpdate;
	private Delta deltaRender;
	private Timer timerUpdate;
	private Timer timerRender;
	private Timer timerProfile;
	private int fpsLimit;

	private boolean initialized;

	/**
	 * Carries out the setup for basic framework components and the framework. Call {@link #run()} after creating a instance.
	 *
	 * @param unlocalizedName The name to be used when determining where the roaming save files are saved.
	 * @param fpsLimit The limit to FPS, (-1 disables limits).
	 * @param extensions The extensions to load for the framework.
	 */
	public Framework(String unlocalizedName, int fpsLimit, Extension... extensions) {
		Framework.INSTANCE = this;

		// Loads some simple framework runtime info.
		loadFlounderStatics(unlocalizedName);
		super.setName("framework");

		// Increment revision every fix for the minor version release. Minor version represents the build month. Major incremented every two years OR after major core framework rewrites.
		this.version = new Version("21.02.11");

		// Sets basic framework info.
		this.closedRequested = false;
		this.startTime = System.nanoTime();

		// Sets up the module and extension managers.
		this.modulesActive = new ArrayList<>();
		this.extensionsChanged = true;

		// Registers these modules as global, we do this as everyone loves these guys <3
		registerModules(loadModule(FlounderLogger.class));
		registerModules(loadModule(FlounderProfiler.class));

		// Force registers the extensions, as the framework was null when they were constructed.
		for (Extension extension : extensions) {
			extension.getExtendedModule().registerExtension(extension);
		}

		// Creates variables to be used for timing updates and renders.
		this.deltaUpdate = new Delta();
		this.deltaRender = new Delta();
		this.timerUpdate = new Timer(1.0 / 60.0);
		this.timerRender = new Timer(Math.abs(1.0 / (double) fpsLimit));
		this.timerProfile = new Timer(1.0 / 7.5);
		this.fpsLimit = fpsLimit;

		// Sets the framework as initialized.
		this.initialized = false;
	}

	/**
	 * Called before the framework loads, used to setup roaming folders and other statics.
	 *
	 * @param unlocalizedName The implementations name, used to set the roaming save folder.
	 */
	private void loadFlounderStatics(String unlocalizedName) {
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

	/**
	 * Gets if the framework contains a module.
	 *
	 * @param object The module class.
	 *
	 * @return If the framework contains a module.
	 */
	protected static boolean containsModule(Class object) {
		for (Module m : INSTANCE.modulesActive) {
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
	protected static void registerModule(Module module) {
		if (module == null || containsModule(module.getClass())) {
			return;
		}

		// Add the module temporally.
		INSTANCE.modulesActive.add(module);

		// Will load and register required modules if needed.
		if (!containsModules(module.getRequires())) {
			// Registers all required modules.
			registerModules(loadModules(module.getRequires()));

			// Add the module to the modules list.
			INSTANCE.modulesActive.remove(module);
			INSTANCE.modulesActive.add(module);
		}

		// Initialize modules if needed,
		if (Framework.isInitialized() && !module.isInitialized()) {
			module.init();
			module.setInitialized(true);
		}
	}

	/**
	 * Registers a list of modules, and initializes them if the engine has already started.
	 *
	 * @param modules The list of modules to register.
	 */
	protected static void registerModules(Module... modules) {
		for (Module module : modules) {
			registerModule(module);
		}
	}

	/**
	 * Forces the framework to reevaluate extension usage within modules. This should be called a extensions active state changes.
	 */
	public static void forceChange() {
		INSTANCE.extensionsChanged = true;
	}

	/**
	 * Gets if the extensions list needs to be reevaluated.
	 *
	 * @return If the extensions list needs to be reevaluated.
	 */
	public static boolean isChanged() {
		return INSTANCE.extensionsChanged;
	}

	@Override
	public void run() {
		try {
			initialize();

			while (isRunning()) {
				update();
				profile();
				sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
			FlounderLogger.exception(e);
			System.exit(-1);
		} finally {
			dispose();
		}
	}

	/**
	 * Function used to initialize the framework.
	 */
	private void initialize() {
		if (!initialized) {
			// Initializes all modules.
			for (Module module : INSTANCE.modulesActive) {
				if (!module.isInitialized()) {
					module.init();
					module.setInitialized(true);
				}
			}

			// Logs all registered modules.
			for (Module module : INSTANCE.modulesActive) {
				// Log module data.
				String requires = "";

				for (int i = 0; i < module.getRequires().length; i++) {
					requires += module.getRequires()[i].getSimpleName() + ((i == module.getRequires().length - 1) ? "" : ", ");
				}

				FlounderLogger.register("Registering " + module.getClass().getSimpleName() + ":" + FlounderLogger.ANSI_PURPLE + " (" + (Framework.isInitialized() ? "POST_INIT, " : "") + module.getModuleUpdate().name() + ")" + FlounderLogger.ANSI_RESET + ":" + FlounderLogger.ANSI_RED + " Requires(" + requires + ")" + FlounderLogger.ANSI_RESET);
			}

			// Logs initialize times.
			FlounderLogger.log("Framework Initialize & Load Time: " + FlounderLogger.ANSI_RED + ((System.nanoTime() - startTime) / 1000000000.0) + FlounderLogger.ANSI_RESET + " seconds!");

			// Sets the framework as initialized.
			initialized = true;
		}
	}

	/**
	 * Function used to update the framework.
	 */
	private void update() {
		if (!initialized) {
			return;
		}

		// Updates the module when needed always.
		for (Module module : INSTANCE.modulesActive) {
			if (module.getModuleUpdate().equals(Module.ModuleUpdate.UPDATE_ALWAYS)) {
				module.getProfileTimer().startInvocation();
				module.update();
				module.getProfileTimer().stopInvocation();
				module.getProfileTimer().reset();
			}
		}

		// Updates when needed.
		if (timerUpdate.isPassedTime()) {
			// Updates the frameworks delta.
			deltaUpdate.update();

			// Updates the modules when needed before the entrance.
			for (Module module : INSTANCE.modulesActive) {
				if (module.getModuleUpdate().equals(Module.ModuleUpdate.UPDATE_PRE)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}

			// Updates the modules when needed after the entrance.
			for (Module module : INSTANCE.modulesActive) {
				if (module.getModuleUpdate().equals(Module.ModuleUpdate.UPDATE_POST)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}

			// Resets the timer.
			timerUpdate.resetStartTime();
		}

		// Renders when needed.
		if (timerRender.isPassedTime() || fpsLimit <= 0) {
			// Updates the render delta, and render time extension.
			deltaRender.update();

			// Updates the module when needed after the rendering.
			for (Module module : INSTANCE.modulesActive) {
				if (module.getModuleUpdate().equals(Module.ModuleUpdate.UPDATE_RENDER)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}

			// Resets the timer.
			timerRender.resetStartTime();
		}

		extensionsChanged = false;
	}

	/**
	 * Function used to profile the framework.
	 */
	private void profile() {
		if (!initialized) {
			return;
		}

		// Profile some values to the logger.
		if (timerProfile.isPassedTime()) {
			// Profile the framework, modules, and extensions.
			if (FlounderProfiler.isOpen()) {
				FlounderProfiler.add(PROFILE_TAB_NAME, "Running From Jar", runningFromJar);
				FlounderProfiler.add(PROFILE_TAB_NAME, "Save Folder", roamingFolder.getPath());
				FlounderProfiler.add(PROFILE_TAB_NAME, "Frames Per Second", Maths.roundToPlace(1.0f / getDeltaRender(), 3));
				FlounderProfiler.add(PROFILE_TAB_NAME, "Updates Per Second", Maths.roundToPlace(1.0f / getDelta(), 3));

				// Profiles the module, also adding its profile timer values.
				for (Module module : INSTANCE.modulesActive) {
					FlounderProfiler.add(module.getProfileTab(), "Update Time", module.getProfileTimer().getFinalTime());
					module.profile();
				}
			}

			//	FlounderLogger.log(Maths.roundToPlace(1.0f / getDelta(), 2) + "ups, " + Maths.roundToPlace(1.0f / getDeltaRender(), 2) + "fps");
			timerProfile.resetStartTime();
		}
	}

	/**
	 * Function used to sleep the framework for 1 millisecond.
	 */
	private void sleep() {
		// Sleep a bit after updating or rendering.
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Gets the frameworks current version.
	 *
	 * @return The frameworks current version.
	 */
	public static Version getVersion() {
		return INSTANCE.version;
	}

	/**
	 * Gets the delta (seconds) between updates.
	 *
	 * @return The deltaRender between updates.
	 */
	public static float getDelta() {
		return (float) INSTANCE.deltaUpdate.getDelta();
	}

	/**
	 * Gets the delta (seconds) between renders.
	 *
	 * @return The delta between renders.
	 */
	public static float getDeltaRender() {
		return (float) INSTANCE.deltaRender.getDelta();
	}

	/**
	 * Gets the current FPS limit.
	 *
	 * @return The current FPS limit.
	 */
	public static int getFpsLimit() {
		return INSTANCE.fpsLimit;
	}

	/**
	 * Sets a limit to the fps, (-1 disabled limits).
	 *
	 * @param fpsLimit The FPS limit.
	 */
	public static void setFpsLimit(int fpsLimit) {
		INSTANCE.fpsLimit = fpsLimit;
		INSTANCE.timerRender.setInterval(Math.abs(1.0f / fpsLimit));
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in milliseconds.
	 */
	public static float getTimeMs() {
		return (System.nanoTime() - INSTANCE.startTime) / 1000000.0f; // The dividend can be used as a time scalar.
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in seconds.
	 */
	public static float getTimeSec() {
		return (System.nanoTime() - INSTANCE.startTime) / 1000000000.0f;
	}

	/**
	 * Gets if the framework still running.
	 *
	 * @return Is the framework still running?
	 */
	public static boolean isRunning() {
		return !INSTANCE.closedRequested;
	}

	/**
	 * Requests the implementation-loop to stop and the implementation to exit.
	 */
	public static void requestClose() {
		INSTANCE.closedRequested = true;
	}

	/**
	 * Gets if the framework is currently initialized.
	 *
	 * @return Is the framework is currently initialized?
	 */
	public static boolean isInitialized() {
		return INSTANCE != null && INSTANCE.initialized;
	}

	/**
	 * Gets if the framework is currently running from a jar.
	 *
	 * @return Is the framework is currently running from a jar?
	 */
	public static boolean isRunningFromJar() {
		return INSTANCE.runningFromJar;
	}

	/**
	 * Gets the file that goes to the roaming folder.
	 *
	 * @return The roaming folder file.
	 */
	public static MyFile getRoamingFolder() {
		return INSTANCE.roamingFolder;
	}

	/**
	 * Gets the current framework instance.
	 *
	 * @return The current instance.
	 */
	public static Framework getInstance() {
		return INSTANCE;
	}

	/**
	 * Disposed the framework if initialised.
	 */
	private void dispose() {
		if (initialized) {
			FlounderLogger.warning("Disposing framework! A new Framework object must be recreated if resetting the framework!");

			Collections.reverse(modulesActive);
			for (Module module : INSTANCE.modulesActive) {
				if (module.isInitialized()) {
					module.dispose();
					module.setInitialized(false);
				}
			}

			modulesActive.clear();
			closedRequested = false;
			initialized = false;

			INSTANCE = null;
		}
	}
}
