package flounder.parsing;

import flounder.resources.*;

import java.util.*;

public class Configuration {
	private Map<Section, List<ConfigData>> dataMap;
	private MyFile configSaveFile;

	public Configuration(MyFile configSaveFile) {
		this.dataMap = new HashMap<>();
		this.configSaveFile = configSaveFile;
	}

	public void load() {

	}

	public void save() {

	}

	public Map<Section, List<ConfigData>> getDataMap() {
		return dataMap;
	}

	public MyFile getConfigSaveFile() {
		return configSaveFile;
	}

	public static enum Section {
		GENERAL("TODO"), DEBUG("TODO"), GRAPHICS("TODO"), CONTROLS("TODO"), WORLD("TODO");

		protected final String description;

		Section(String description) {
			this.description = description;
		}
	}

	public static class ConfigData {

	}
}
