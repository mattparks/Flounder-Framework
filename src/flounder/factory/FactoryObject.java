package flounder.factory;

/**
 * The object that the factory will create.
 */
public class FactoryObject {
	/**
	 * Creates a new factory object.
	 */
	protected FactoryObject() {
	}

	/**
	 * Creates a new factory builder.
	 *
	 * @param base The base information for the builder.
	 *
	 * @return A new factory builder.
	 */
	public static FactoryBuilder newModel(FactoryBase base) {
		return new FactoryBuilder(base);
	}

	/**
	 * Creates a new empty factory object.
	 *
	 * @return A new empty factory object.
	 */
	public static FactoryObject getEmptyModel() {
		return new FactoryObject();
	}
}
