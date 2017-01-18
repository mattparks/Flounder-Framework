package flounder.standard;

import flounder.framework.*;
import flounder.profiling.*;

import java.util.*;

/**
 * A module used for managing simple update injection standards.
 */
public class FlounderStandard extends IModule {
	private static final FlounderStandard INSTANCE = new FlounderStandard();
	public static final String PROFILE_TAB_NAME = "Standard";

	private List<IStandard> standards;

	/**
	 * Creates a new standard manager.
	 */
	public FlounderStandard() {
		super(ModuleUpdate.UPDATE_PRE, PROFILE_TAB_NAME);
	}

	@Override
	public void init() {
		standards = new ArrayList<>();
	}

	@Override
	public void update() {
		List<IExtension> newStandards = getExtensions();

		if (newStandards != null) {
			List<IStandard> newCasted = new ArrayList<>();
			newStandards.forEach(extension -> newCasted.add(((IStandard) extension)));

			if (standards != null) {
				List<IStandard> removedStandards = new ArrayList<>();
				removedStandards.addAll(standards);
				removedStandards.removeAll(newCasted);

				removedStandards.forEach(removed -> {
					removed.dispose();
					removed.setInitialized(false);
				});
			} else {
				standards = new ArrayList<>();
			}

			standards.clear();
			standards.addAll(newCasted);

			standards.forEach(standard -> {
				if (!standard.isInitialized()) {
					standard.init();
					standard.setInitialized(true);
				}
			});
		}

		if (standards != null && !standards.isEmpty()) {
			standards.forEach(IStandard::update);
		}
	}

	@Override
	public void profile() {
		if (standards != null && !standards.isEmpty()) {
			standards.forEach(IStandard::update);
			standards.forEach(IStandard::profile);
		}

		FlounderProfiler.add(PROFILE_TAB_NAME, "Standards", standards.size());
	}

	@Override
	public IModule getInstance() {
		return INSTANCE;
	}

	@Override
	public void dispose() {
		if (standards != null && !standards.isEmpty()) {
			standards.forEach(standard -> {
				standard.dispose();
				standard.setInitialized(false);
			});
			standards.clear();
		}
	}
}
