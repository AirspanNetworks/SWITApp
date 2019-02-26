package com.swit;

import static com.swit.SwitMailPlugin.AUTOMATION_VERSION;
import static com.swit.SwitMailPlugin.BRANCH;
import static com.swit.SwitMailPlugin.DESCRIPTION;
import static com.swit.SwitMailPlugin.SCENARIO_NAME;
import static com.swit.SwitMailPlugin.SETUP_NAME;
import static com.swit.SwitMailPlugin.TARGET_VERSION;
import static com.swit.SwitMailPlugin.VERSION_PATH;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class MailBodyUtils {
	private static final Logger log = LoggerFactory.getLogger(MailBodyUtils.class);
	private static Templates templates = new Templates();
	
	private static final String UNEXPECTED_REBOOTS = "Unexpected Reboots";
	private static final String SEGMENTATION_FAULT = "Segmentation fault";
	private static final String MIDDLE_OF_CORE = "middle of core dumping";
	private static final String CORE_CARE = "Corecare";
	private static final String PHY_ASSERT_DETECTED = "PHY ASSERT DETECTED";
	
	public static StringBuilder getSubject() {
		StringBuilder subject = new StringBuilder();
		return subject;
	}
	
	public static String getTitle(ExecutionMetadata metadata, Map<String, String> scenarioProperties, List<ElasticsearchTest> tests) {
		//Format: {Scenario_Name} results for Version {TargetVersion} X/Y : PASS/FAIL/Core Warning
		String title = ""; String status;
		int successTests = metadata.getNumOfSuccessfulTests();
		int warningTests = metadata.getNumOfTestsWithWarnings();
		int numOfAllTests = metadata.getNumOfTests();
		
		String scenarioName = scenarioProperties.get(SCENARIO_NAME);
		String scenarioNameWithSpace = scenarioName.replaceAll("(.)([A-Z])", "$1 $2");
		
		String targetVersion = scenarioProperties.get(TARGET_VERSION);
		
		status = ExecutionUtils.getStatus(metadata, tests);
		
		title = templates.populateTitleTemplate(scenarioNameWithSpace, targetVersion, (successTests + warningTests), numOfAllTests, status);
		
		return title;
	}


	public static String getBodyVersionsAndLinks(Map<String, String> scenarioProperties, List<ElasticsearchTest> tests, String uri, String executionDuration) {
		String bodyVersionsAndLinks = "";
		String urlFirstTest = tests.get(0).getUrl();
		String indexLink = urlFirstTest.split("/")[0] + urlFirstTest.split("/")[2] + "/" + uri;
		String startTime = tests.get(tests.size()-1).getExecutionTimeStamp();
		String time = startTime.split(" ")[1] + " UTC";
		String date = startTime.split(" ")[0];
		
		//convert yyyy/mm/dd -> dd/mm/yyyy
		date = convertDateFormat(date, "yyyy/MM/dd", "dd/MM/yyyy");
		
		if ((null != scenarioProperties.get(AUTOMATION_VERSION))
				|| (null != scenarioProperties.get(BRANCH))
				|| (null != scenarioProperties.get(VERSION_PATH))
				|| (null != scenarioProperties.get(DESCRIPTION))
				|| !ExecutionUtils.getAllDevicesVersionsFromTestsProperties(tests).isEmpty()) {

			String scenarioName = scenarioProperties.get(SCENARIO_NAME);
			String scenarioNameWithSpace = scenarioName.replaceAll("(.)([A-Z])", "$1 $2");
			
			bodyVersionsAndLinks = templates.populateVersionsAndLinksTemplate(scenarioNameWithSpace, scenarioProperties.get(AUTOMATION_VERSION),
					scenarioProperties.get(BRANCH), scenarioProperties.get(VERSION_PATH),
					scenarioProperties.get(DESCRIPTION),
					ExecutionUtils.getAllDevicesVersionsFromTestsProperties(tests), indexLink, date, time, executionDuration, ExecutionUtils.getAllDevicesDetails(scenarioProperties));
		}

		return bodyVersionsAndLinks;
	}
	
	public static String convertDateFormat(String date, String fromFormat, String toFormat){
		SimpleDateFormat originalFormatter = new SimpleDateFormat (fromFormat);
		SimpleDateFormat newFormatter = new SimpleDateFormat (toFormat);

		// parsing date string using original format
		ParsePosition pos = new ParsePosition(0);
		Date dateFromString = originalFormatter.parse(date, pos);
		
		// Now you have a date object and you can convert it to the new format
		String res = newFormatter.format(dateFromString);
		
		return res;
	}

	public static String getSetupConfigurationTable(Map<String, String> scenarioProperties) {
		String setupConfigurationTable = "";
		if (!ExecutionUtils.getSetupConfiguration(scenarioProperties).isEmpty()) {
			setupConfigurationTable = templates.populateSetupConfigurationTableTemplate(
					scenarioProperties.get(SETUP_NAME),
					ExecutionUtils.getSetupConfiguration(scenarioProperties));
		}
		return setupConfigurationTable;
	}

	public static String getSummary(ExecutionStatus currentExecutionStatus, List<ElasticsearchTest> tests, String uri) {
		String summary = "";
		String urlFirstTest = tests.get(0).getUrl();
		String indexLink = urlFirstTest.split("/")[0] + urlFirstTest.split("/")[2] + "/" + uri;

		summary = templates.populateStatusTemplate(indexLink, currentExecutionStatus);

		return summary;
	}

	public static String getComparisonBetweenTwoExecutions(ExecutionStatus currentExecutionStatus, int executionId,
			String executionType) throws Exception {
		String comparison = "";
		int previousExecutionId = ExecutionUtils.findPreviousExecutionId(executionId, executionType);
		List<ElasticsearchTest> tests = ExecutionUtils.getAllTestsOfExecution(executionId);
		if (0 != previousExecutionId) {
			String previousTargetVersion = ExecutionUtils.getExecutionScenarioProperties(previousExecutionId).get(TARGET_VERSION);
			List<ElasticsearchTest> previousTests = ExecutionUtils.getAllTestsOfExecution(previousExecutionId);
			ExecutionStatus previousExecutionStatus;
			previousExecutionStatus = ExecutionStatus.caluculateExecutionStatuses(previousTests, previousExecutionId, executionType, previousTargetVersion);
			log.debug("Status of previous execution with id " + previousExecutionId + " is " + previousExecutionStatus);
			ComparisonResult comparisonResult;
			comparisonResult = ComparisonResult.compareExecutions(tests, previousTests);

			comparison = templates.populateComparisonTemplate(currentExecutionStatus, previousExecutionStatus,
					comparisonResult);

		}
		return comparison;
	}

	public static String getTestsFearuresTable(List<ElasticsearchTest> tests, int executionId, String executionType, String targetVersion) {
		String fearures = "";
		ExecutionStatus allExecutionStatus = ExecutionStatus.caluculateExecutionStatuses(tests, executionId, executionType, targetVersion);
		Map<String, FeatureStatus> fearuresMap = ExecutionUtils.caluculateAllStatusesPerFeaturesOfExecution(tests, executionId, targetVersion);
		fearures = templates.populateStatuesPerFearuresTemplate(allExecutionStatus, fearuresMap);
		return fearures;
	}

	public static String getTestsTable(List<ElasticsearchTest> tests) {
		String strTests = "";
		strTests = templates.populateTestsTemplate(ExecutionUtils.getTestsOrderByEnb(tests));

		return strTests;
	}
	
	public static String getErrorsAndWarningsTable(String logCounter) {
		String errorsAndWarnings = "";
		if(logCounter == null) {
			return "";
		}
		Map<String, String> lineExpressionKeys = ExecutionUtils.getAllLineExpressionKeys(logCounter);
		
		if (!lineExpressionKeys.isEmpty()) {
			Map<String, Map<String, Map<String, String>>> upMaps = new TreeMap<String, Map<String, Map<String, String>>>();
			Map<String, Map<String, Map<String, String>>> downMaps = new TreeMap<String, Map<String, Map<String, String>>>();
			
			Map<String, Map<String, Map<String, String>>> allMaps = ExecutionUtils.getLineExpressionMapBySeverity(lineExpressionKeys); 
			for(String key1 : allMaps.keySet()) {
				for(String key2 : allMaps.get(key1).keySet()) {
					if(key2.contains(UNEXPECTED_REBOOTS)
							|| key2.contains(SEGMENTATION_FAULT)
							|| key2.contains(MIDDLE_OF_CORE)
							|| key2.contains(CORE_CARE)
							|| key2.contains(PHY_ASSERT_DETECTED)) {
						upMaps.put(key1, allMaps.get(key1));
					}
					else {
						downMaps.put(key1, allMaps.get(key1));
					}
				}
			}
			errorsAndWarnings = templates.populateEnbsErrorsAndWarningsTemplate(
					upMaps, downMaps, ExecutionUtils.getAllEnbsNamesLogExpression(logCounter));
		}
		return errorsAndWarnings;
	}
	
	public static String getErrorsAndWarningsTableOldFormat(Map<String, String> scenarioProperties) {
		String errorsAndWarnings = "";
		Map<String, String> lineExpressionKeys = ExecutionUtils.getAllLineExpressionKeysOldFormat(scenarioProperties);
		
		if (!lineExpressionKeys.isEmpty()) {
			Map<String, Map<String, Map<String, String>>> upMaps = new TreeMap<String, Map<String, Map<String, String>>>();
			Map<String, Map<String, Map<String, String>>> downMaps = new TreeMap<String, Map<String, Map<String, String>>>();
			
			Map<String, Map<String, Map<String, String>>> allMaps = ExecutionUtils.getLineExpressionMapBySeverity(lineExpressionKeys); 
			for(String key1 : allMaps.keySet()) {
				for(String key2 : allMaps.get(key1).keySet()) {
					if(key2.contains(UNEXPECTED_REBOOTS)
							|| key2.contains(SEGMENTATION_FAULT)
							|| key2.contains(MIDDLE_OF_CORE)
							|| key2.contains(CORE_CARE)
							|| key2.contains(PHY_ASSERT_DETECTED)) {
						upMaps.put(key1, allMaps.get(key1));
					}
					else {
						downMaps.put(key1, allMaps.get(key1));
					}
				}
			}
			errorsAndWarnings = templates.populateEnbsErrorsAndWarningsTemplate(
					upMaps, downMaps, ExecutionUtils.getAllEnbsNamesLogExpressionOldFormat(scenarioProperties));
		}
		return errorsAndWarnings;
	}

	public static String getGraphs(List<ElasticsearchTest> tests) {
		String graphs = "";
		if (!ExecutionUtils.getAllGraphsLinks(tests).isEmpty()) {
			graphs = templates.populateGraphsTemplate(ExecutionUtils.getAllGraphsLinks(tests));
		}
		else if (!ExecutionUtils.getAllGraphsLinksOldFormat(tests).isEmpty()) {
			graphs = templates.populateGraphsTemplate(ExecutionUtils.getAllGraphsLinksOldFormat(tests));
		}
		return graphs;
	}
}