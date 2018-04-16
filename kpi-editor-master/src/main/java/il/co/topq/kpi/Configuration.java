package il.co.topq.kpi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public enum Configuration {

	INSTANCE;

	public enum ConfigProps {

		// @formatter:off
		DOC_ROOT_FOLDER("doc.root.folder", "docRoot"),
		ELASTIC_HOST("elastic.host","localhost"),
		ELASTIC_HTTP_PORT("elastic.http.port","9200");
		// @formatter:off

		private final String propName;

		private final String defaultValue;

		private ConfigProps(String value, String defaultValue) {
			this.propName = value;
			this.defaultValue = defaultValue;
		}

		public String getPropName() {
			return propName;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

	}

	private final Logger log = LoggerFactory.getLogger(Configuration.class);

	private final static String CONFIG_PROP_NAME = "config.properties";

	private Properties configProperties = new Properties();

	private Configuration() {
		if (!new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME).exists()) {
			useDefaultProperties();
			return;
		}
		readConfigurationFromFile();
		if (configProperties.isEmpty()) {
			useDefaultProperties();
		}

	}

	private void readConfigurationFromFile() {
		try (FileReader reader = new FileReader(new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME))) {
			configProperties.load(reader);

		} catch (Exception e) {
			log.warn("Failure in reading file " + CONFIG_PROP_NAME + ". Rolling back to default properties", e);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Object key : configProperties.keySet()) {
			sb.append(key).append(":").append(configProperties.getProperty(String.valueOf(key))).append("\n");
		}
		return sb.toString();
	}

	private void useDefaultProperties() {
		log.info("No configuration file found - Creating one with default parameters in "
				+ new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME).getAbsolutePath());
		for (ConfigProps prop : ConfigProps.values()){
			addPropWithDefaultValue(prop);
		}
		try (FileOutputStream out = new FileOutputStream(
				new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME))) {
			configProperties.store(out, "Default difido server properties");
		} catch (Exception e) {
			log.warn("Failed writing default configuration file", e);
		}
	}

	private void addPropWithDefaultValue(ConfigProps configProp) {
		configProperties.put(configProp.getPropName(), configProp.getDefaultValue());
	}

	public boolean readBoolean(ConfigProps prop) {
		return !"false".equals(readString(prop));
	}

	public int readInt(ConfigProps prop) {
		final String value = readString(prop);
		if (value != null && !value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public List<String> readList(ConfigProps prop) {
		final String value = configProperties.getProperty(prop.getPropName());
		if (StringUtils.isEmpty(value)) {
			return new ArrayList<String>();
		}
		return Arrays.asList(value.split(";"));
	}

	public String readString(ConfigProps prop) {
		final String value = configProperties.getProperty(prop.getPropName());
		if (null == value) {
			return prop.getDefaultValue();
		}
		return value.trim();
	}
	
}