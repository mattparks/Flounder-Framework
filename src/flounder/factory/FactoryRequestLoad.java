package flounder.factory;

import flounder.processing.opengl.*;
import flounder.processing.resource.*;

/**
 * A class that can process a request to load a factory object.
 */
public class FactoryRequestLoad implements RequestResource, RequestOpenGL {
	private Factory factory;
	private FactoryObject object;
	private FactoryBuilder builder;

	/**
	 * Creates a new factory load request.
	 *
	 * @param factory The factory to use when executing requests.
	 * @param builder The builder to load from.
	 * @param object The object to load into.
	 */
	protected FactoryRequestLoad(Factory factory, FactoryObject object, FactoryBuilder builder) {
		this.factory = factory;
		this.object = object;
		this.builder = builder;
	}

	@Override
	public void executeRequestResource() {
		// Loads resource data into the object.
		factory.loadData(object, builder);
	}

	@Override
	public void executeRequestGL() {
		while (!object.isLoaded()) {
			// Wait for resources to load into data...
		}

		// Creates the object and sets as loaded.
		factory.create(object);
		object.setLoaded();
	}
}