package com.swit;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.Common;

public class Configuration {

	private final Logger log = LoggerFactory.getLogger(Configuration.class);
	public static String MAIL_CONFIG_FILE_NAME = "trends_mail_plugin.properties";
	public static String JIRA_CONFIG_FILE_NAME = "jira_update_plugin.properties";
	private static Configuration instance;
	private Properties properties;

	private Configuration() {
		// Singleton
	}
	
	private void readConfigurationFromFile(String filename) {
		properties = new Properties();
		try (FileReader reader = new FileReader(new File(Common.CONFIUGRATION_FOLDER_NAME, filename))) {
			properties.load(reader);

		} catch (Exception e) {
			log.warn("Failure in reading file " + filename + ". Rolling back to default properties", e);
		}
	}

	public static Configuration getInstance(String fileName) {
		if (null == instance) {
			instance = new Configuration();
		}
		instance.readConfigurationFromFile(fileName);
		return instance;
	}

	public boolean readBoolean(String property) {
		return !"false".equals(readString(property));
	}

	public int readInt(String property) {
		final String value = readString(property);
		if (value != null && !value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public List<String> readList(String property) {
		final String value = readString(property);
		if (StringUtils.isEmpty(value)) {
			return new ArrayList<String>();
		}
		return Arrays.asList(value.split(";"));
	}

	public String readString(String property) {
		final String value = properties.getProperty(property);
		if (null == value) {
			return "";
		}
		return value.trim();
	}
}