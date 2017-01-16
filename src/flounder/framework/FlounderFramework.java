package flounder.framework;

import flounder.logger.*;
import flounder.maths.*;
import flounder.maths.Timer;
import flounder.profiling.*;
import flounder.resources.*;

import java.io.*;
import java.util.*;

import static flounder.framework.FlounderModules.*;

// TODO: Profile module update timings using ProfileTimer.

/**
 * A framework used for simplifying the creation of complicated Java applications. By using flexible Module loading and Extension injecting, it allows the engine to be used for Networking, Imaging, AIs, Games, and many more applications.
 * Start off by creating a new FlounderFramework object in your main thread, using Extensions in the constructor. By using Extensions: Modules can be required and therefor loaded into the framework.
 * Implementing interfaces like {@link flounder.standard.IStandard} with your extension can allow you do task specific things with your Extensions. After creating your Framework object call {@link #run()} to start.
 */
public class FlounderFramework extends Thread {
	private static FlounderFramework instance;

	private static boolean runningFromJar;
	private static MyFile roamingFolder;

	private Version version;

	private boolean closedRequested;
	private long startTime;

	private Delta deltaUpdate;
	private Delta deltaRender;
	private Timer timerUpdate;
	private Timer timerRender;
	private Timer timerLog;
	private int fpsLimit;

	private boolean initialized;

	/**
	 * Carries out the setup for basic framework components and the framework. Call {@link #run()} after creating a instance.
	 *
	 * @param unlocalizedName The name to be used when determining where the roaming save files are saved.
	 * @param fpsLimit The limit to FPS, (-1 disables limits).
	 * @param extensions The extensions to load for the framework.
	 */
	public FlounderFramework(String unlocalizedName, int fpsLimit, IExtension... extensions) {
		FlounderFramework.instance = this;
		loadFlounderStatics(unlocalizedName);

		// Increment revision every fix for the minor version release. Minor version represents the build month. Major incremented every two years OR after major core framework rewrites.
		this.version = new Version("15.01.10");

		for (IExtension extension : extensions) {
			// Should already be registered...
		}

		this.closedRequested = false;
		this.startTime = System.nanoTime();

		this.deltaUpdate = new Delta();
		this.deltaRender = new Delta();
		this.timerUpdate = new Timer(1.0 / 60.0);
		this.timerRender = new Timer(Math.abs(1.0 / (double) fpsLimit));
		this.timerLog = new Timer(1L);
		this.fpsLimit = fpsLimit;

		this.deltaUpdate.update();
		this.deltaRender.update();

		FlounderModules.registerModules(FlounderModules.loadModules(FlounderLogger.class));

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
			e.printStackTrace();
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
				FlounderLogger.log("Framework Initialize & Load Time: " + FlounderLogger.ANSI_RED + ((System.nanoTime() - startTime) / 1000000000.0) + FlounderLogger.ANSI_RESET + " seconds!");
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
			if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_ALWAYS)) {
				module.update();
			}
		});

		// Updates when needed.
		if (timerUpdate.isPassedTime()) {
			// Updates the frameworks delta.
			deltaUpdate.update();

			// Updates the modules when needed before the entrance.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_PRE)) {
					module.update();
				}
			});

			// Updates the modules when needed after the entrance.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_POST)) {
					module.update();
				}
			});

			// Resets the timer.
			timerUpdate.resetStartTime();
		}

		// Renders when needed.
		if (timerRender.isPassedTime() || fpsLimit < 0) {
			// Updates the render delta, and render time extension.
			deltaRender.update();

			// Updates the module when needed after the rendering.
			modulesActive.forEach((module) -> {
				if (module.getModuleUpdate().equals(IModule.ModuleUpdate.UPDATE_RENDER)) {
					module.update();
				}
			});

			// Resets the timer.
			timerRender.resetStartTime();
		}

		FlounderModules.extensionsChanged = false;
	}

	/**
	 * Function used to profile the framework.
	 */
	private void profile() {
		// Profile some values to the logger.
		if (timerLog.isPassedTime()) {
			//	FlounderLogger.log(Maths.roundToPlace(1.0f / getDelta(), 2) + "ups, " + Maths.roundToPlace(1.0f / getDeltaRender(), 2) + "fps");
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
		return (float) instance.deltaUpdate.getDelta();
	}

	/**
	 * Gets the delta (seconds) between renders.
	 *
	 * @return The delta between renders.
	 */
	public static float getDeltaRender() {
		return (float) instance.deltaRender.getDelta();
	}

	/**
	 * Gets the current FPS limit.
	 *
	 * @return The current FPS limit.
	 */
	public static int getFpsLimit() {
		return instance.fpsLimit;
	}

	/**
	 * Sets a limit to the fps, (-1 disabled limits).
	 *
	 * @param fpsLimit The FPS limit.
	 */
	public static void setFpsLimit(int fpsLimit) {
		instance.fpsLimit = fpsLimit;
		instance.timerRender.setInterval(Math.abs(1.0f / fpsLimit));
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in milliseconds.
	 */
	public static float getTimeMs() {
		return (System.nanoTime() - instance.startTime) / 1000000.0f; // The dividend can be used as a time scalar.
	}

	/**
	 * Gets the current time of the framework instance.
	 *
	 * @return The current framework time in seconds.
	 */
	public static float getTimeSec() {
		return (System.nanoTime() - instance.startTime) / 1000000000.0f;
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

	/**
	 * Disposed the framework if initialised.
	 */
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

			modulesActive.clear();
			modulesUnlogged.clear();
			closedRequested = false;
			initialized = false;
		}
	}
}
