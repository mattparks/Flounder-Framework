package flounder.profiling;

import flounder.framework.*;
import flounder.logger.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * A module used for profiling many parts of the framework.
 */
public class FlounderProfiler extends Module {
	private static final String PROFILER_TITLE = "Flounder Framework Profiler";

	private FlounderTabMenu primaryTabMenu;
	private JFrame profilerJFrame;
	private boolean profilerOpen;

	/**
	 * Creates a new new profiler.
	 */
	public FlounderProfiler() {
		super(FlounderLogger.class);
	}

	@Handler.Function(Handler.FLAG_INIT)
	public void init() {
	}

	@Handler.Function(Handler.FLAG_UPDATE_ALWAYS)
	public void update() {
		if (profilerOpen && profilerJFrame == null) {
			this.profilerJFrame = new JFrame(PROFILER_TITLE);
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

		}

		if (profilerJFrame != null && profilerJFrame.isVisible() != profilerOpen) {
			profilerJFrame.setVisible(profilerOpen);
		}
	}

	@Handler.Function(Handler.FLAG_PROFILE)
	public void profile() {
		FlounderProfiler.get().add(getTab(), "Is Open", profilerOpen);
	}

	/**
	 * Adds a value to a tab.
	 *
	 * @param tabName The tabs name to add to.
	 * @param title The title of the label.
	 * @param value The value to add with the title.
	 * @param <T> The type of value to add.
	 */
	public <T> void add(String tabName, String title, T value) {
		if (this.primaryTabMenu == null) {
			return;
		}

		addTab(tabName); // Forces the tab to be there.
		FlounderProfilerTab tab = this.primaryTabMenu.getCategoryComponent(tabName).get();
		tab.addLabel(title, value); // Adds the label to the tab.
	}

	/**
	 * Adds a tab by name to the menu if it does not exist.
	 *
	 * @param tabName The tab name to add.
	 */
	public void addTab(String tabName) {
		if (this.primaryTabMenu == null) {
			return;
		}

		if (!this.primaryTabMenu.doesCategoryExist(tabName)) {
			this.primaryTabMenu.createCategory(tabName);
		}
	}

	/**
	 * Gets if the profiler is open.
	 *
	 * @return If the profiler is open.
	 */
	public boolean isOpen() {
		return this.profilerOpen;
	}

	/**
	 * Toggles the visibility of the JFrame.
	 *
	 * @param open If the JFrame should be open.
	 */
	public void toggle(boolean open) {
		this.profilerOpen = open;
	}

	@Handler.Function(Handler.FLAG_DISPOSE)
	public void dispose() {
		this.profilerOpen = false;

		if (primaryTabMenu != null) {
			primaryTabMenu.dispose();
			primaryTabMenu = null;
		}

		if (profilerJFrame != null) {
			profilerJFrame.dispose();
			profilerJFrame = null;
		}
	}

	@Module.Instance
	public static FlounderProfiler get() {
		return (FlounderProfiler) Framework.getInstance(FlounderProfiler.class);
	}

	@Module.TabName
	public static String getTab() {
		return "Profiler";
	}
}
