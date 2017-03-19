package flounder.parsing;

/**
 * A config section that has data written into it.
 */
public enum ConfigSection {
	GENERAL("Configs that are generic in nature"), DEBUG("Configs used for debugging"), GRAPHICS("Configs that effect graphics"), CONTROLS("Configs used to setup control types"), WORLD("Configs saved relating to a world");

	protected final String description;

	ConfigSection(String description) {
		this.description = description;
	}
}
