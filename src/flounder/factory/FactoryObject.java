package flounder.factory;

/**
 * The object the factory will be loading into.
 */
public abstract class FactoryObject {
	private boolean loaded = false;

	/**
	 * Creates a new empty factory object.
	 */
	public FactoryObject() {
		loaded = true;
	}

	/**
	 * Gets if the information has been loaded into the object.
	 *
	 * @return If the object is loaded.
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * Sets that the factory has been loaded.
	 */
	protected void setLoaded() {
		loaded = true;
	}
}
