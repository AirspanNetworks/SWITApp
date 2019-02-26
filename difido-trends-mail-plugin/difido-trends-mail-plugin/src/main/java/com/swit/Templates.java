package com.swit;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public final class Templates {
	private static final Logger log = LoggerFactory.getLogger(Templates.class);

	private String resourceToString(final String resourceName) {
		try (Scanner s = new Scanner(getClass().getClassLoader().getResourceAsStream(resourceName))) {
			s.useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
	}

	public String populateTitleTemplate(String scenarioName, String targetVersion, int passTests, int allTests,
			String status) {
		log.debug("Populating title template");
		VelocityContext context = new VelocityContext();
		context.put("scenarioName", scenarioName);
		context.put("targetVersion", targetVersion);
		context.put("passTests", passTests);
		context.put("allTests", allTests);
		context.put("status", status);
		return populateTemplate(context, "title.vm");
	}

	public String populateVersionsAndLinksTemplate(String scenarioName, String version, String branch,
			String versionPath, String description, Map<String, String> devices, String link, String date, String time,
			String duration, Map<String, Map<String, String>> devicesDetails) {
		log.debug("Populating versions and links template");
		VelocityContext context = new VelocityContext();
		context.put("scenarioName", scenarioName);
		context.put("version", version);
		context.put("branch", branch);
		context.put("versionPath", versionPath);
		context.put("description", description);
		context.put("devices", devices);
		context.put("link", link);
		context.put("date", date);
		context.put("time", time);
		context.put("duration", duration);
		context.put("devicesDetails", devicesDetails);
		return populateTemplate(context, "versionsAndLinks.vm");
	}

	public String populateSetupConfigurationTableTemplate(String title, Map<String, String> setupConfiguration) {
		log.debug("Populating Setup Configuration template");
		VelocityContext context = new VelocityContext();
		context.put("title", title);
		context.put("setupConfiguration", setupConfiguration);
		return populateTemplate(context, "setupConfigurationTable.vm");
	}

	public String populateStatusTemplate(String link, ExecutionStatus currentExecutionStatus) {
		log.debug("Populating status template for execution " + currentExecutionStatus.getId());
		VelocityContext context = new VelocityContext();
		context.put("current", currentExecutionStatus);
		context.put("link", link);
		return populateTemplate(context, "status.vm");
	}

	public String populateComparisonTemplate(ExecutionStatus currentExecutionStatus,
			ExecutionStatus previousExecutionStatus, ComparisonResult comparisonResult) {
		log.debug("Populating comparing template for executions " + currentExecutionStatus.getId() + " and "
				+ previousExecutionStatus.getId());
		VelocityContext context = new VelocityContext();
		context.put("current", currentExecutionStatus);
		context.put("previous", previousExecutionStatus);
		context.put("comparison", comparisonResult);
		return populateTemplate(context, "comparison.vm");
	}

	public String populateStatuesPerFearuresTemplate(ExecutionStatus allTests,
			Map<String, FeatureStatus> statusPerFeatureMap) {
		log.debug("Populating statues per fearures template");
		VelocityContext context = new VelocityContext();
		context.put("allTests", allTests);
		context.put("statusPerFeatureMap", statusPerFeatureMap);
		return populateTemplate(context, "statusesPerFeaturesTable.vm");
	}

	public String populateTestsTemplate(Map<List<ElasticsearchTest>, List<String>> testsEnbsMap) {
		log.debug("Populating tests by enbs template");
		VelocityContext context = new VelocityContext();
		context.put("testsEnbsMap", testsEnbsMap);
		context.put("StringUtils", org.apache.commons.lang3.StringUtils.class);
		return populateTemplate(context, "tests.vm");
	}

	public String populateEnbsErrorsAndWarningsTemplate(Map<String, Map<String, Map<String, String>>> upMaps,
			Map<String, Map<String, Map<String, String>>> downMaps, Set<String> enbs) {
		log.debug("Populating enbs errors and enbs warnings template");
		VelocityContext context = new VelocityContext();
		context.put("enbs", enbs);
		context.put("redMaps", upMaps);
		context.put("whiteMaps", downMaps);
		return populateTemplate(context, "enbsErrorsAndWarningsTable.vm");
	}

	public String populateGraphsTemplate(Map<String, String> graphs) {
		log.debug("Populating graphs template");
		VelocityContext context = new VelocityContext();
		context.put("graphs", graphs);
		return populateTemplate(context, "graphs.vm");
	}

	public String populateTemplate(VelocityContext context, String templateName) {
		VelocityEngine ve = new VelocityEngine();
		ve.init();
		final StringWriter writer = new StringWriter();
		ve.evaluate(context, writer, "mailPlugin", resourceToString(templateName));
		return writer.toString();
	}
}