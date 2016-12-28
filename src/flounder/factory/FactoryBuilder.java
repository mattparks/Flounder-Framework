package flounder.factory;

import flounder.logger.*;
import flounder.processing.*;

import java.lang.ref.*;

/**
 * A builder used to set parameters for loading.
 */
public abstract class FactoryBuilder {
	private Factory factory;
	private String name;

	/**
	 * Creates a new builder.
	 *
	 * @param factory The factory to be used with.
	 */
	protected FactoryBuilder(Factory factory) {
		this.factory = factory;
		this.name = "";
	}

	/**
	 * Sets the name to be referenced by.
	 *
	 * @param name The name to be referenced by.
	 *
	 * @return This.
	 */
	public FactoryBuilder setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Creates a new factory object, carries out the CPU loading, and then runs on the OpenGL thread.
	 *
	 * @return The factory object that has been created.
	 */
	public FactoryObject create() {
		SoftReference<FactoryObject> ref = factory.getLoaded().get(name);
		FactoryObject object = ref == null ? null : ref.get();

		if (object == null) {
			FlounderLogger.log(name + " is being loaded into the " + factory.getFactoryName() + " factory right now!");
			factory.getLoaded().remove(name);
			object = factory.newObject();
			FlounderProcessors.sendRequest(new FactoryRequestLoad(factory, object, this));
			factory.getLoaded().put(name, new SoftReference<>(object));
		}

		return object;
	}
}
