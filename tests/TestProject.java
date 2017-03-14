import flounder.framework.*;
import flounder.framework.updater.*;
import flounder.logger.*;
import flounder.maths.*;
import flounder.standards.*;

/**
 * The class that contains the main method.
 */
public class TestProject {
	public static void main(String[] args) {
		// Creates a new framework object.
		Framework framework = new Framework("test", new UpdaterDefault(), -1, new TestInterface());

		// Runs the frameworks thread.
		framework.run();

		// After close, exits the programs.
		System.exit(0);
	}

	/**
	 * The programs interface, this one is used for a simple close countdown.
	 */
	public static class TestInterface extends Standard {
		private static final int INTERVAL_CLOSE = 60;

		private Timer timer;
		private int i;

		public TestInterface() {
			super(FlounderLogger.class);
		}

		@Override
		public void init() {
			FlounderLogger.log("TestInterface initialized!");

			this.timer = new Timer(1.0);
			this.i = 0;
		}

		@Override
		public void update() {
			// Called in the update pre loop. Framework update order: Always, /Pre/, Post, Render.

			// A simple close countdown.
			if (timer.isPassedTime()) {
				i++;

				if (i == INTERVAL_CLOSE) {
					FlounderLogger.log("TestInterface requesting close!");
					Framework.requestClose();
				} else {
					//	FlounderLogger.log("TestInterface closing after: " + (INTERVAL_CLOSE - i) + " seconds!");
				}

				timer.resetStartTime();
			}
		}

		@Override
		public void profile() {
			// Called after every update, if the profiler is open.
		}

		@Override
		public void dispose() {
			FlounderLogger.log("TestInterface disposed!");
		}

		@Override
		public boolean isActive() {
			return true;
		}
	}
}