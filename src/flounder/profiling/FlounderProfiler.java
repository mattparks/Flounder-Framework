package flounder.profiling;

import flounder.framework.*;
import flounder.logger.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * A module used for profiling many parts of the framework.
 */
public class FlounderProfiler extends Module {
	private static final FlounderProfiler INSTANCE = new FlounderProfiler();
	public static final String PROFILE_TAB_NAME = "Profiler";

	private JFrame profilerJFrame;
	private FlounderTabMenu primaryTabMenu;
	private boolean profilerOpen;

	/**
	 * Creates a new new profiler.
	 */
	public FlounderProfiler() {
		super(ModuleUpdate.UPDATE_ALWAYS, PROFILE_TAB_NAME, FlounderLogger.class);
	}

	@Override
	public void init() {
		String title = "Flounder Framework Profiler";
		this.profilerJFrame = new JFrame(title);
		profilerJFrame.setSize(420, 720);
		profilerJFrame.setResizable(true);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
			ex.printStackTrace();
		}

		profilerJFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		profilerJFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(profilerJFrame,
						"Are you sure to close this profiler?", "Really Closing?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					profilerOpen = false;
				}
			}
		});

		this.primaryTabMenu = new FlounderTabMenu();
		this.profilerJFrame.add(primaryTabMenu);

		// Opens the profiler if not running from jar.
		// toggle(!Framework.isRunningFromJar());
	}

	@Override
	public void update() {
		if (profilerJFrame.isVisible() != profilerOpen) {
			profilerJFrame.setVisible(profilerOpen);
		}
	}

	@Override
	public void profile() {
		FlounderProfiler.add(PROFILE_TAB_NAME, "Is Open", profilerOpen);
	}

	/**
	 * Toggles the visibility of the JFrame.
	 *
	 * @param open If the JFrame should be open.
	 */
	public static void toggle(boolean open) {
		INSTANCE.profilerOpen = open;
	}

	/**
	 * Adds a value to a tab.
	 *
	 * @param tabName The tabs name to add to.
	 * @param title The title of the label.
	 * @param value The value to add with the title.
	 * @param <T> The type of value to add.
	 */
	public static <T> void add(String tabName, String title, T value) {
		if (INSTANCE.primaryTabMenu == null) {
			return;
		}

		addTab(tabName); // Forces the tab to be there.
		FlounderProfilerTab tab = INSTANCE.primaryTabMenu.getCategoryComponent(tabName).get();
		tab.addLabel(title, value); // Adds the label to the tab.
	}

	/**
	 * Adds a tab by name to the menu if it does not exist.
	 *
	 * @param tabName The tab name to add.
	 */
	public static void addTab(String tabName) {
		if (!INSTANCE.primaryTabMenu.doesCategoryExist(tabName)) {
			INSTANCE.primaryTabMenu.createCategory(tabName);
		}
	}

	/**
	 * Gets if the profiler is open.
	 *
	 * @return If the profiler is open.
	 */
	public static boolean isOpen() {
		return INSTANCE.profilerOpen;
	}

	@Override
	public Module getInstance() {
		return INSTANCE;
	}

	@Override
	public void dispose() {
		this.profilerOpen = false;
		primaryTabMenu.dispose();
		profilerJFrame.dispose();
	}
}
