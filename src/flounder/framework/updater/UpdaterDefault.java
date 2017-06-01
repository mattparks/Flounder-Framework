package flounder.framework.updater;

import flounder.framework.*;
import flounder.logger.*;
import flounder.maths.*;
import flounder.maths.Timer;

import java.util.*;

/**
 * The default updater for the framework.
 */
public class UpdaterDefault implements IUpdater {
	private TimingReference timing;
	private double startTime;

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
		this.startTime = System.nanoTime() * 1e-9;

		if (timing != null) {
			this.startTime = timing.getTime();
		}

		// Creates variables to be used for timing updates and renders.
		this.timeOffset = 0.0f;
		this.deltaUpdate = new Delta();
		this.deltaRender = new Delta();
		this.timerUpdate = new Timer(1.0 / 60.0);
		this.timerRender = new Timer(1.0 / 60.0);
		this.timerProfile = new Timer(1.0 / 2.0);
	}

	@Override
	public void run() {
		initialize();

		while (Framework.isRunning()) {
			if (Framework.isInitialized()) {
				update();
				profile();
			}
		}
	}

	private void initialize() {
		if (Framework.isInitialized()) {
			return;
		}

		// Initializes all modules.
		Framework.runHandlers(Handler.FLAG_INIT);

		// Logs initialize times.
		FlounderLogger.get().init("Framework Initialize & Load Time: " + FlounderLogger.ANSI_RED + (getTimeSec() - startTime) + FlounderLogger.ANSI_RESET + " seconds!");

		// Sets the framework as initialized.
		Framework.setInitialized(true);
	}

	private void update() {
		// Updates the module when needed always.
		Framework.runHandlers(Handler.FLAG_UPDATE_ALWAYS);

		// Updates when needed.
		if (timerUpdate.isPassedTime()) {
			// Resets the timer.
			timerUpdate.resetStartTime();

			// Updates the frameworks delta.
			deltaUpdate.update();

			// Updates the modules when needed before the entrance.
			Framework.runHandlers(Handler.FLAG_UPDATE_PRE);

			// Updates the modules when needed after the entrance.
			Framework.runHandlers(Handler.FLAG_UPDATE_POST);

			for (Module module : Framework.getModules()) {
				module.setExtensionChanged(false);
			}
		}

		// Renders when needed.
		if ((timerRender.isPassedTime() || Framework.getFpsLimit() == -1 || Framework.getFpsLimit() > 1000.0f) && Maths.almostEqual(timerUpdate.getInterval(), deltaUpdate.getDelta(), 10.0)) {
			// Resets the timer.
			timerRender.resetStartTime();

			// Updates the render delta, and render time extension.
			deltaRender.update();

			// Updates the module when needed after the rendering.
			Framework.runHandlers(Handler.FLAG_RENDER);
		}
	}

	private void profile() {
		if (!Framework.isInitialized()) {
			return;
		}

		// Profile some values to the profiler.
		if (timerProfile.isPassedTime()) {
			//	FlounderLogger.get().log(Maths.roundToPlace(1.0f / getDelta(), 2) + "ups, " + Maths.roundToPlace(1.0f / getDeltaRender(), 2) + "fps");
			timerProfile.resetStartTime();
		}
	}

	@Handler.Function(Handler.FLAG_DISPOSE)
	public void dispose() {
		if (!Framework.isInitialized()) {
			return;
		}

		FlounderLogger.get().warning("Disposing framework!"); // A new Framework object must be recreated if resetting the framework!

		Collections.reverse(Framework.getModules());
		Framework.runHandlers(Handler.FLAG_DISPOSE);

		Framework.getModules().clear();
		Framework.setInitialized(false);
	}

	@Override
	public void setTiming(TimingReference timing) {
		this.timing = timing;
		this.startTime = timing.getTime();
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

		return (float) (time - startTime) + timeOffset;
	}

	@Override
	public float getTimeMs() {
		return getTimeSec() * 1000.0f;
	}
}
