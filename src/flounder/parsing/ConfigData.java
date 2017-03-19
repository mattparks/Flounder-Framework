package flounder.parsing;

public class ConfigData implements Comparable<ConfigData> {
	protected String key;
	protected String data;
	protected String description;

	protected ConfigReference reference;

	public ConfigData(String key, String data, String description, ConfigReference reference) {
		this.key = fixDataString(key);
		this.data = fixDataString(data);
		this.description = fixDataString(description);

		this.reference = reference;
	}

	private String fixDataString(String string) {
		return string.replace("#", "").replace("$", "").replace(",", "").replace(";", "").replace("{", "").replace("}", "");
	}

	public String getString() {
		return data;
	}

	public boolean getBoolean() {
		return Boolean.parseBoolean(data);
	}

	public int getInteger() {
		return Integer.parseInt(data);
	}

	public double getDouble() {
		return Double.parseDouble(data);
	}

	public float getFloat() {
		return Float.parseFloat(data);
	}

	@Override
	public int compareTo(ConfigData object) {
		return this.key.compareTo(object.key);
	}
}
