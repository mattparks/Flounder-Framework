package flounder.parsing;

import flounder.helpers.*;
import flounder.logger.*;
import flounder.resources.*;

import java.io.*;
import java.util.*;

public class Config {
	private Map<ConfigSection, List<ConfigData>> dataMap;
	private MyFile file;

	public Config(MyFile file) {
		this.dataMap = new HashMap<>();

		for (ConfigSection section : ConfigSection.values()) {
			this.dataMap.put(section, new ArrayList<>());
		}

		this.file = file;

		load();
	}

	public void load() {
		File saveFile = insureFile();

		try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
			String line;

			ConfigSection currentSection = null;

			while ((line = br.readLine()) != null) {
				line = line.trim();

				if (line.startsWith("#")) {
					currentSection = null;
					String section = line.substring(1, line.length()).split("\\(")[0].trim();

					for (ConfigSection s : ConfigSection.values()) {
						if (s.name().equals(section)) {
							currentSection = s;
						}
					}
				} else if (line.startsWith("$")) {
					String key = line.substring(1, line.length()).split("\\(")[0].trim();
					String data = line.split(":")[1].trim();
					String description = line.split(":")[0];
					description = description.substring(description.indexOf("(") + 1);
					description = description.substring(0, description.indexOf(")"));
					this.dataMap.get(currentSection).add(new ConfigData(key, data, description, null));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public <T> ConfigData getData(ConfigSection section, String key, T defaultData, String defaultDescription) {
		return getData(section, key, defaultData, defaultDescription, null);
	}

	public <T> ConfigData getData(ConfigSection section, String key, T defaultData, String defaultDescription, ConfigReference reference) {
		for (ConfigData data : dataMap.get(section)) {
			if (data.key.equals(key)) {
				// Fix descriptions and references.
				if (!data.description.equals(defaultDescription)) {
					data.description = defaultDescription;
				}

				if (data.reference == null) {
					data.reference = reference;
				}

				// The data loaded.
				return data;
			}
		}

		ConfigData configData = new ConfigData(key, defaultData.toString(), defaultDescription, reference);
		dataMap.get(section).add(configData);
		return configData;
	}

	public void save() {
		try {
			File saveFile = insureFile();

			FileWriter fileWriter = new FileWriter(saveFile);
			FileWriterHelper fileWriterHelper = new FileWriterHelper(fileWriter);

			for (ConfigSection section : dataMap.keySet()) {
				fileWriterHelper.beginNewSegment("#" + section.name() + "(\'" + section.description + "\'):", false);

				for (ConfigData data : ArraySorting.insertionSort(dataMap.get(section))) {
					String save = data.reference == null ? data.data : data.reference.getReading().toString();
					fileWriterHelper.writeSegmentData("$" + data.key + "(\'" + data.description + "\'): " + save, true);
				}

				fileWriterHelper.endSegment(true, false);
			}

			// Closes the file for writing.
			fileWriter.close();
		} catch (IOException e) {
			FlounderLogger.error("File saver for config " + file.getName() + " did not save successfully!");
			FlounderLogger.exception(e);
		}
	}

	private File insureFile() {
		File saveDirectory = new File(file.getPath().replaceAll(file.getName(), "").substring(1));
		File saveFile = new File(file.getPath().substring(1));

		if (!saveDirectory.exists()) {
			System.out.println("Creating directory: " + saveDirectory);

			try {
				saveDirectory.mkdir();
			} catch (SecurityException e) {
				System.out.println("Filed to create " + file.getPath() + " folder.");
				e.printStackTrace();
			}
		}

		if (!saveFile.exists()) {
			System.out.println("Creating file: " + saveFile);

			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Filed to create " + file.getPath() + " file.");
				e.printStackTrace();
			}
		}

		return saveFile;
	}

	public Map<ConfigSection, List<ConfigData>> getDataMap() {
		return dataMap;
	}

	public MyFile getFile() {
		return file;
	}
}
