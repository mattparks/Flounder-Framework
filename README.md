# Flounder-Framework
Flounder Framework is a modular Java framework composed of modules and extensions.

# Modules
A module (IModule) is a class that is built to serve a specific purpose, for example: the processor module only acts to manage requests.
When they are updated in the main iterative loop is determined from the IModule.ModuleUpdate enum they pass through the super constructor.
Modules may depend on other modules, for example: the profiler requires the logger. A requirement list if built within the framework.

# Extensions
A extension (IExtension) is a class that is used to added extra functionality to a module. For example: a camera extension could be used to define a camera.
The only requirement for a extension is for the module to support extensions. Creating a extension for the logger will not do anything!
The extension may depend on other modules, and while it is active, the module will be a forced requirement (if not already depended on).

# Creating a Project
You must in some way create a new FlounderFramework object, and also create a basic standard extension (we call them the programs interface).
The interface will require module, and can be used to load configs, initialize display settings, create worlds, etc...

<pre>
    import flounder.framework.*;
    import flounder.logger.*;
    import flounder.maths.*;
    import flounder.standards.*;

    /**
     * The class that contains the main method.
     */
    public class TestProject {
        public static void main(String[] args) {
            // Creates a new framework object.
            FlounderFramework framework = new FlounderFramework("test", -1, new TestInterface());

            // Runs the frameworks thread.
            framework.run();

            // After close, exits the programs.
            System.exit(0);
        }

        /**
         * The programs interface, this one is used for a simple close countdown.
         */
        public static class TestInterface extends IStandard {
            private static final int INTERVAL_CLOSE = 5;

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
                        FlounderFramework.requestClose();
                    } else {
                        FlounderLogger.log("TestInterface closing after: " + (INTERVAL_CLOSE - i) + " seconds!");
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
</pre>