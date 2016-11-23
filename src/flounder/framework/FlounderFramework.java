package flounder.framework;

import flounder.logger.*;
import flounder.maths.*;
import flounder.maths.Timer;
import flounder.profiling.*;
import flounder.resources.*;

import java.io.*;
import java.util.*;

import static flounder.framework.FlounderModules.*;

/**
 * Deals with much of the initializing, updating, and cleaning up of the framework.
 */
public class FlounderFramework extends Thread {
	private static FlounderFramework instance;

	private Version version;
	private List<IExtension> extensions;

	private boolean closedRequested;
	private long startTime;

	private Delta deltaUpdate;
	private Delta deltaRender;
	private Timer timerUpdate;
	private Timer timerLog;

	private boolean initialized;
	private static boolean runningFromJar;
	private static MyFile roamingFolder;

	/**
	 * Carries out the setup for basic framework components and the framework. Call {@link #run()} after creating a instance.
	 *
	 * @param unlocalizedName The name to be used when determining where the roaming save files are saved.
	 * @param extensions The extensions to load for the framework.
	 */
	public FlounderFramework(String unlocalizedName, IExtension... extensions) {
		FlounderFramework.instance = this;
		loadFlounderStatics(unlocalizedName);

		// Increment revision every fix for the minor version release. Minor version represents the build month. Major incremented every two years OR after major core framework rewrites.
		this.version = new Version("1.11.23");
		this.extensions = new ArrayList<>(Arrays.asList(extensions));

		this.closedRequested = false;
		this.startTime = System.currentTimeMillis();

		this.deltaUpdate = new Delta();
		this.deltaRender = new Delta();
		this.timerUpdate = new Timer(1.0f / 60.0f);
		this.timerLog = new Timer(1.0f);

		this.deltaUpdate.update();
		this.deltaRender.update();

		this.initialized = false;
	}

	/**
	 * Called before the framework loads, used to setup roaming folders and other statics.
	 *
	 * @param unlocalizedName The implementations name, used to set the roaming save folder.
	 */
	private void loadFlounderStatics(String unlocalizedName) {
		runningFromJar = FlounderFramework.class.getResource("/" + FlounderFramework.class.getName().replace('.', '/') + ".class").toString().startsWith("jar:");
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
			modulesActive.forEach((module) -> {
				if (!module.isInitialized()) {
					module.init();
					module.setInitialized(true);
				}
			});

			if (containsModule(FlounderLogger.class)) {
				if (!modulesUnlogged.isEmpty()) {
					modulesUnlogged.forEach(FlounderLogger::register);
				}

				modulesUnlogged.clear();
			}

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
		modulesActive.forEach((module) -> {
			if (module.getModuleUpdate().equals(IModule.ModuleUpdate.ALWAYS)) {
				module.run();
			}
		});

		// Updates when needed.
		if (timerUpdate.isPassedTime()) {
			// Updates the frameworks delta.
			deltaUpdate.update();

			// Updates the modules when needed before the entrance.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_PRE)) {
					module.run();
				}
			});

			// Updates the modules when needed after the entrance.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_POST)) {
					module.run();
				}
			});

			// Resets the timer.
			timerUpdate.resetStartTime();
		}

		// Renders when needed.
		if (true) { // TODO: Limit when rendering!
			// Updates the render delta, and render time extension.
			deltaRender.update();

			// Updates the module when needed after the rendering.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.RENDER)) {
					module.run();
				}
			});
		}
	}

	/**
	 * Function used to profile the framework.
	 */
	private void profile() {
		// Profile some values to the logger.
		if (timerLog.isPassedTime()) {
			FlounderLogger.log(Maths.roundToPlace(1.0f / getDelta(), 2) + "fps");
			timerLog.resetStartTime();
		}

		// Profile the framework, modules, and extensions.
		if (FlounderProfiler.isOpen()) {
			FlounderProfiler.add("Framework", "Running From Jar", runningFromJar);
			FlounderProfiler.add("Framework", "Save Folder", roamingFolder.getPath());

			modulesActive.forEach(IModule::profile);
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
	 * Gets a list of all currently active extensions.
	 *
	 * @return All active extensions.
	 */
	public static List<IExtension> getExtensions() {
		return instance.extensions;
	}

	/**
	 * Gets the frameworks current version.
	 *
	 * @return The frameworks current version.
	 */
	public static Version getVersion() {
		return instance.version;
	}

	/**
	 * Gets the delta (seconds) between updates.
	 *
	 * @return The deltaRender between updates.
	 */
	public static float getDelta() {
		return instance.deltaUpdate.getDelta();
	}

	/**
	 * Gets the current framework time (all delta added up).
	 *
	 * @return The current framework time.
	 */
	public static float getDeltaTime() {
		return instance.deltaUpdate.getTime();
	}

	/**
	 * Gets the delta (seconds) between renders.
	 *
	 * @return The delta between renders.
	 */
	public static float getDeltaRender() {
		return instance.deltaRender.getDelta();
	}

	/**
	 * Gets the current framework time (all delta added up).
	 *
	 * @return The current framework time.
	 */
	public static float getDeltaRenderTime() {
		return instance.deltaRender.getTime();
	}

	/**
	 * Gets if the framework still running?
	 *
	 * @return Is the framework still running?
	 */
	public static boolean isRunning() {
		return !instance.closedRequested;
	}

	/**
	 * Requests the implementation-loop to stop and the implementation to exit.
	 */
	public static void requestClose() {
		instance.closedRequested = true;
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time (milliseconds).
	 */
	public static float getTime() {
		return System.currentTimeMillis() - instance.startTime; // FlounderDisplay.getTime();
	}

	/**
	 * Gets if the framework is currently initialized.
	 *
	 * @return Is the framework is currently initialized?
	 */
	public static boolean isInitialized() {
		return instance != null && instance.initialized;
	}

	/**
	 * Gets if the framework is currently running from a jae.
	 *
	 * @return Is the framework is currently running from a jae?
	 */
	public static boolean isRunningFromJar() {
		return runningFromJar;
	}

	/**
	 * Gets the file that goes to the roaming folder.
	 *
	 * @return The roaming folder file.
	 */
	public static MyFile getRoamingFolder() {
		return roamingFolder;
	}

	private void dispose() {
		if (initialized) {
			FlounderLogger.warning("Disposing framework! A new FlounderFramework object must be recreated if resetting the framework!");

			Collections.reverse(modulesActive);
			modulesActive.forEach((module) -> {
				if (module.isInitialized()) {
					module.dispose();
					module.setInitialized(false);
				}
			});

			extensions.clear();
			modulesActive.clear();
			modulesUnlogged.clear();
			closedRequested = false;
			initialized = false;
		}
	}
}
