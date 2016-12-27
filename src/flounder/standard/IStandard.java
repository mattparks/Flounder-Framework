package flounder.standard;

import flounder.framework.*;

/**
 * A extension used with {@link flounder.standard.FlounderStandard} to define a standard.
 */
public abstract class IStandard extends IExtension {
	/**
	 * Creates a new standard.
	 *
	 * @param requires The classes that are extra requirements for this implementation.
	 */
	public IStandard(Class... requires) {
		super(FlounderStandard.class, requires);
	}

	/**
	 * Run when initializing the standard.
	 */
	public abstract void init();

	/**
	 * Run when updating the standard.
	 */
	public abstract void update();

	/**
	 * Run when profiling the standard.
	 */
	public abstract void profile();

	/**
	 * Run when disposing the standard.
	 */
	public abstract void dispose();

	@Override
	public abstract boolean isActive();
}
