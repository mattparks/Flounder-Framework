package flounder.framework.updater;

import flounder.framework.*;
import flounder.logger.*;
import flounder.maths.*;
import flounder.maths.Timer;
import flounder.profiling.*;

import java.util.*;

/**
 * The default updater for the framework.
 */
public class UpdaterDefault implements IUpdater {
	private TimingReference timing;
	private long startTime;

	private float timeOffset;
	private Delta deltaUpdate;
	private Delta deltaRender;
	private Timer timerUpdate;
	private Timer timerRender;
	private Timer timerProfile;

	public UpdaterDefault(TimingReference timing) {
		// Sets the timing for the updater to run from.
		this.timing = timing;

		// Sets basic updater info.
		this.startTime = 0; // System.nanoTime()

		// Creates variables to be used for timing updates and renders.
		this.timeOffset = 0.0f;
		this.deltaUpdate = new Delta();
		this.deltaRender = new Delta();
		this.timerUpdate = new Timer(1.0 / 64.0);
		this.timerRender = new Timer(1.0 / 60.0);
		this.timerProfile = new Timer(1.0 / 5.0);
	}

	@Override
	public void initialize() {
		if (Framework.isInitialized()) {
			return;
		}

		// Initializes all modules.
		for (Module module : Framework.getModulesActive()) {
			if (!module.isInitialized()) {
				module.init();
				module.setInitialized(true);
			}
		}

		// Logs all registered modules.
		for (Module module : Framework.getModulesActive()) {
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
		Framework.setInitialized(true);
	}

	@Override
	public void update() {
		if (!Framework.isInitialized()) {
			return;
		}

		// Updates the module when needed always.
		for (Module module : Framework.getModulesActive()) {
			if (module.getModuleUpdate().equals(ModuleUpdate.UPDATE_ALWAYS)) {
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

			// Resets the timer.
			timerUpdate.resetStartTime();

			// Updates the modules when needed before the entrance.
			for (Module module : Framework.getModulesActive()) {
				if (module.getModuleUpdate().equals(ModuleUpdate.UPDATE_PRE)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}

			// Updates the modules when needed after the entrance.
			for (Module module : Framework.getModulesActive()) {
				if (module.getModuleUpdate().equals(ModuleUpdate.UPDATE_POST)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}
		}

		// Renders when needed.
		if ((timerRender.isPassedTime() || Framework.getFpsLimit() <= 0) && Maths.almostEqual(timerUpdate.getInterval(), deltaUpdate.getDelta(), 6.0)) {
			// Updates the render delta, and render time extension.
			deltaRender.update();

			// Resets the timer.
			timerRender.resetStartTime();

			// Updates the module when needed after the rendering.
			for (Module module : Framework.getModulesActive()) {
				if (module.getModuleUpdate().equals(ModuleUpdate.UPDATE_RENDER)) {
					module.getProfileTimer().startInvocation();
					module.update();
					module.getProfileTimer().stopInvocation();
					module.getProfileTimer().reset();
				}
			}
		}
	}

	@Override
	public void profile() {
		if (!Framework.isInitialized()) {
			return;
		}

		// Profile some values to the profiler.
		if (timerProfile.isPassedTime()) {
			//	FlounderLogger.log(Maths.roundToPlace(1.0f / getDelta(), 2) + "ups, " + Maths.roundToPlace(1.0f / getDeltaRender(), 2) + "fps");
			timerProfile.resetStartTime();

			// Profile the framework, modules, and extensions.
			if (FlounderProfiler.isOpen()) {
				FlounderProfiler.add(Framework.PROFILE_TAB_NAME, "Running From Jar", Framework.isRunningFromJar());
				FlounderProfiler.add(Framework.PROFILE_TAB_NAME, "Save Folder", Framework.getRoamingFolder().getPath());
				FlounderProfiler.add(Framework.PROFILE_TAB_NAME, "Frames Per Second", Maths.roundToPlace(1.0f / getDeltaRender(), 3));
				FlounderProfiler.add(Framework.PROFILE_TAB_NAME, "Updates Per Second", Maths.roundToPlace(1.0f / getDelta(), 3));

				// Profiles the module, also adding its profile timer values.
				for (Module module : Framework.getModulesActive()) {
					FlounderProfiler.add(module.getProfileTab(), "Update Time", module.getProfileTimer().getFinalTime());
					module.profile();
				}
			}
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

	@Override
	public void dispose() {
		if (!Framework.isInitialized()) {
			return;
		}

		FlounderLogger.warning("Disposing framework! A new Framework object must be recreated if resetting the framework!");

		Collections.reverse(Framework.getModulesActive());
		for (Module module : Framework.getModulesActive()) {
			if (module.isInitialized()) {
				module.dispose();
				module.setInitialized(false);
			}
		}

		Framework.getModulesActive().clear();
		Framework.setInitialized(false);
	}

	@Override
	public float getTimeOffset() {
		return timeOffset;
	}

	@Override
	public void setTimeOffset(float timeOffset) {
		this.timeOffset = timeOffset;
	}

	@Override
	public float getDelta() {
		return (float) deltaUpdate.getDelta();
	}

	@Override
	public float getDeltaRender() {
		return (float) deltaRender.getDelta();
	}

	@Override
	public void setFpsLimit(float fpsLimit) {
		this.timerRender.setInterval(Math.abs(1.0f / fpsLimit));
	}

	@Override
	public float getTimeSec() {
		double time = System.nanoTime() * 1e-9;

		if (timing != null) {
			time = timing.getTime();
		}

		return ((float) time - startTime) + timeOffset;
	}

	@Override
	public float getTimeMs() {
		return getTimeSec() * 1000.0f;
	}

	/**
	 * A reference to a time fetching function
	 */
	public interface TimingReference<T> {
		/**
		 * Gets the time from the function.
		 *
		 * @return The time read in seconds.
		 */
		double getTime();
	}
}
