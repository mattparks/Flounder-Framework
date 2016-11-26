package flounder.standard;

import flounder.framework.*;
import flounder.logger.*;
import flounder.profiling.*;

import java.util.*;

/**
 * A module used for managing simple update injection standards.
 */
public class FlounderStandard extends IModule {
	private static final FlounderStandard instance = new FlounderStandard();

	private List<IStandard> standards;

	/**
	 * Creates a new standard manager.
	 */
	public FlounderStandard() {
		super(ModuleUpdate.UPDATE_PRE, FlounderLogger.class, FlounderProfiler.class);
	}

	@Override
	public void init() {
		standards = new ArrayList<>();
	}

	@Override
	public void run() {
		List<IExtension> newStandards = FlounderFramework.getExtensionMatches(IStandard.class, true);

		if (newStandards != null) {
			List<IStandard> newCasted = new ArrayList<>();
			newStandards.forEach(extension -> newCasted.add(((IStandard) extension)));

			if (standards != null) {
				List<IStandard> removedStandards = new ArrayList<>();
				removedStandards.addAll(standards);
				removedStandards.removeAll(newCasted);

				removedStandards.forEach(removed -> {
					removed.dispose();
					((IExtension) removed).setInitialized(false);
				});
			} else {
				standards = new ArrayList<>();
			}

			standards.clear();
			standards.addAll(newCasted);

			standards.forEach(standard -> {
				if (!((IExtension) standard).isInitialized()) {
					standard.init();
					((IExtension) standard).setInitialized(true);
				}
			});
		}

		if (standards != null && !standards.isEmpty()) {
			standards.forEach(IStandard::update);
		}
	}

	@Override
	public void profile() {
		FlounderProfiler.add("Standards", "Count", standards == null ? "NULL" : standards.size());
	}

	@Override
	public IModule getInstance() {
		return instance;
	}

	@Override
	public void dispose() {
		if (standards != null && !standards.isEmpty()) {
			standards.forEach(standard -> {
				standard.dispose();
				((IExtension) standard).setInitialized(false);
			});
			standards.clear();
		}
	}
}
