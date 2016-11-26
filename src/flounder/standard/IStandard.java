package flounder.standard;

/**
 * A interface used with {@link flounder.framework.IExtension} to define a standard.
 */
public interface IStandard {
	/**
	 * Run when initializing the standard.
	 */
	void init();

	/**
	 * Run when updating the standard (run in the pre-update loop).
	 */
	void update();

	/**
	 * Run when disposing the standard.
	 */
	void dispose();
}
