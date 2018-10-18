package il.co.topq.difido.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import il.co.topq.elastic.ESClient;
import il.co.topq.report.Common;
import il.co.topq.report.Configuration;
import il.co.topq.report.Configuration.ConfigProps;
import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;

import static il.co.topq.difido.plugin.TrendsMailPlugin.*;

public class ExecutionUtils {
	private static final Logger log = LoggerFactory.getLogger(ExecutionUtils.class);

	/**
	 * @param Returns
	 *            true if and only if this execution contains key of test that
	 *            contains specified sequence of char values.
	 * @param s
	 *            the sequence to search for
	 * @return true if exists key that contains s, false otherwise
	 */
	public static boolean existsTestContainsKey(List<ElasticsearchTest> tests, CharSequence s) {
		for (ElasticsearchTest test : tests) {
			if (TestUtils.containsKey(test, s)) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, String> getSetupConfiguration(Map<String, String> scenarioProperties) {
		log.debug("calling getSetupConfiguration method");
		final Map<String, String> setupConfiguration = new LinkedHashMap<String, String>();
		
		if (scenarioProperties.get(RELAY_VERSION) != null) {
			setupConfiguration.put("Relay Version", scenarioProperties.get(RELAY_VERSION));
		}
		if (scenarioProperties.get(UE_TYPE) != null) {
			setupConfiguration.put("UE type", scenarioProperties.get(UE_TYPE));
		}
		if (scenarioProperties.get(NETSPAN_VERSION) != null) {
			setupConfiguration.put("Netspan version", scenarioProperties.get(NETSPAN_VERSION));
		}
		if (scenarioProperties.get(SYSTEM_DEFAULTS_PROFILES) != null) {
			setupConfiguration.put("System defaults profiles", scenarioProperties.get(SYSTEM_DEFAULTS_PROFILES));
		}
		if (scenarioProperties.get(ENB_ADVANCED_CONFIGURATION_PROFILES) != null) {
			setupConfiguration.put("ENB advanced configuration profiles",
					scenarioProperties.get(ENB_ADVANCED_CONFIGURATION_PROFILES));
		}
		if (scenarioProperties.get(CELL_ADVANCED_CONFIGURATION_PROFILES) != null) {
			setupConfiguration.put("Cell advanced configuration profiles",
					scenarioProperties.get(CELL_ADVANCED_CONFIGURATION_PROFILES));
		}
		if (scenarioProperties.get(MOBILITY_PROFILES) != null) {
			setupConfiguration.put("Mobility profiles", scenarioProperties.get(MOBILITY_PROFILES));
		}
		if (scenarioProperties.get(RADIO_PROFILES) != null) {
			setupConfiguration.put("Radio profiles", scenarioProperties.get(RADIO_PROFILES));
		}		
		return setupConfiguration;
	}

	public static Map<String, String> findAllSpecificProperties(Map<String, String> scenarioProperties,
			String containsKey) {
		final Map<String, String> ret = new HashMap<String, String>();

		for (String key : scenarioProperties.keySet()) {
			if (key.contains(containsKey)) {
				ret.put(key, scenarioProperties.get(key));
			}
		}
		return ret;
	}

	public static int getNumberOfTestsWithSpecificStatus(List<ElasticsearchTest> tests, String status) {
		log.debug("calling getNumOfTestsByStatus method");
		int count = 0;
		for (ElasticsearchTest test : tests) {
			if (test.getStatus().equals(status))
				count++;
		}
		return count;
	}

	public static boolean existsTestWithSpecificStatus(List<ElasticsearchTest> tests, String status) {
		log.debug("calling existsTestWithSpecificStatus method");
		for (ElasticsearchTest test : tests) {
			if (test.getStatus().equals(status)) {
				return true;
			}
		}
		return false;
	}
	
	//return e.g: Map<ERROR, Map<Unexpected Reboots, Map<Harmony68, 1>>>
	
	public static Map<String, Map<String, Map<String, String>>> getLineExpressionMapBySeverity(
			Map<String, String> lineExpressionKeys) {
		log.debug("calling getLineExpressionMapBySeverity method");
		Map<String, Map<String, Map<String, String>>> allMaps = new TreeMap<String, Map<String, Map<String, String>>>();
		for (String key : lineExpressionKeys.keySet()) {
			//key e.g: ERROR
			if(key.split("_").length >= 2) {
				allMaps.put(key.split("_")[1], getMapByLineExpression(lineExpressionKeys, key.split("_")[1]));
			}
		}
		return allMaps;
	}

	private static Map<String, Map<String, String>> getMapByLineExpression(Map<String, String> lineExpressionKeys,
			String severity) {
		log.debug("calling getMapByLineExpression method");
		Map<String, Map<String, String>> mapRes = new TreeMap<String, Map<String, String>>();
		for (String lineExpression : ExecutionUtils.getAllLineExpressionAccordingToSeverity(lineExpressionKeys,
				severity)) {
			mapRes.put(lineExpression,getEnbsMapAccordingSpecificLineExpression(lineExpressionKeys, lineExpression, severity));
		}
		return mapRes;
	}

	private static Map<String, String> getEnbsMapAccordingSpecificLineExpression(
			Map<String, String> allEnbsLineExpressionKeys, String lineExpression, String severity) {
		log.debug("calling getEnbsMapAccordingSpecificLineExpression method");
		Map<String, String> enbs = new TreeMap<String, String>();
		for (String key : allEnbsLineExpressionKeys.keySet()) {
			// key e.g: EnbLogExpression_SEVERITY_EnbName(EnbArc)_expression
			// value: sum of Errors/Warnings
			if ((key.split("_").length >= 3) && (key.substring(key.split("_")[0].length() + key.split("_")[1].length() + key.split("_")[2].length() + 3,
					key.length())).equals(lineExpression) && key.split("_")[1].equals(severity)) {
				enbs.put(key.split("_")[2], allEnbsLineExpressionKeys.get(key));
				// enbs.put(enb name, sum of Errors/Warnings)
			}
		}
		// if exists enb without errors/Warnings, it also adds to the list (with count = 0)
		
		for (String key : allEnbsLineExpressionKeys.keySet()) {
			if ((key.split("_").length >= 3) && (!enbs.containsKey(key.split("_")[2]))) {
				enbs.put(key.split("_")[2], "0");
			}
		}
		return enbs;
	}

	public static Map<List<ElasticsearchTest>, List<String>> getTestsOrderByEnb(List<ElasticsearchTest> tests) {
		// List<String> => enb names
		// List<ElasticsearchTest> => tests of enbs
		log.debug("calling getTestsOrderByEnb method");
		Map<List<ElasticsearchTest>, List<String>> result = new LinkedHashMap<List<ElasticsearchTest>, List<String>>();
		List<String> currEnbs = new ArrayList<String>();
		List<ElasticsearchTest> currTests = new ArrayList<ElasticsearchTest>();

		currEnbs = TestUtils.getAllEnbsOfTest(tests.get(0));
		for (ElasticsearchTest test : tests) {
			if (Objects.equal(currEnbs, TestUtils.getAllEnbsOfTest(test))) {
				currTests.add(test);
			} else {
				result.put(currTests, currEnbs);
				currEnbs = new ArrayList<String>();
				currTests = new ArrayList<ElasticsearchTest>();
				currTests.add(test);
				currEnbs = TestUtils.getAllEnbsOfTest(test);
			}
		}
		result.put(currTests, currEnbs);
		return result;
	}
	
	public static Set<String> getAllEnbsNamesLogExpression(String logCounter) {
		log.debug("calling getAllEnbsNamesLogExpression method");
		Set<String> enbs = new TreeSet<String>();

		for (String log : logCounter.split(";")) {
			//log e.g: Velocity13,<enodeb0:SONERROR>,ERROR,1
			enbs.add(log.split(",")[0]);
		}
		return enbs;
	}
	
	public static Set<String> getAllEnbsNamesLogExpressionOldFormat(Map<String, String> scenarioProperties) {
		log.debug("calling getAllEnbsNamesLogExpression method");
		Set<String> enbs = new TreeSet<String>();

		for (String key : scenarioProperties.keySet()) {
			if (key.contains("EnbLogExpression_")) {
				if(key.split("_").length >= 3) {
					enbs.add(key.split("_")[2]);
					// key e.g: EnbLogExpression_SEVERITY_EnbName(EnbArc)_expression
				}
			}
		}
		return enbs;
	}

	public static Set<String> getAllLineExpressionAccordingToSeverity(Map<String, String> lineExpressionKeys,
			String severity) {
		log.debug("calling getAllLineExpressionAccordingToSeverity method");
		Set<String> lineExpression = new LinkedHashSet<String>();
		for (String key : lineExpressionKeys.keySet()) {
			if (key.contains("EnbLogExpression_") && key.contains("_" + severity + "_")) {
				String lineExp = key.substring(
						key.split("_")[0].length() + key.split("_")[1].length() + key.split("_")[2].length() + 3,
						key.length());
				lineExp.replace("<", "");
				lineExp.replace(">", "");
				lineExpression.add(lineExp);
				// we want to connect all the expression
				// key e.g:
				// EnbLogExpression_SEVERITY_EnbName(EnbArc)_expression_expressionContinue1_expressionContinue2
			}
		}
		return lineExpression;
	}
	
	public static Map<String, String> getAllLineExpressionKeys(String logCounter) {
		log.debug("calling getAllLineExpressionKeys method");
		Map<String, String> lineExpressionMap = new LinkedHashMap<String, String>();

		for (String log : logCounter.split(";")) {
			if(log.split(",").length >= 3) {
				String newKey = "EnbLogExpression_"+log.split(",")[2]+"_"+log.split(",")[0]+"_"+log.split(",")[1];
				// key e.g: EnbLogExpression_ERROR_AirVelocity1000PoE(Velocity53)_enodeb0:APP
				// Value e.g: 4
				lineExpressionMap.put(newKey, log.split(",")[3]);
			}
		}
		return lineExpressionMap;
	}
	
	public static Map<String, String> getAllLineExpressionKeysOldFormat(Map<String, String> scenarioProperties) {
		log.debug("calling getAllLineExpressionKeys method");
		Map<String, String> lineExpressionMap = new LinkedHashMap<String, String>();
		for (String key : scenarioProperties.keySet()) {
			if (key.contains("EnbLogExpression_") && (key.split("_").length >= 4)) {
				// key e.g: EnbLogExpression_ERROR_AirVelocity1000PoE(Velocity53)_enodeb0:APP
				// Value e.g: 4
				lineExpressionMap.put(key, scenarioProperties.get(key));
			}
		}
		return lineExpressionMap;
	}

	public static Map<String, String> getAllGraphsLinksOldFormat(List<ElasticsearchTest> tests) {
		log.debug("calling getAllGraphsLinks method");
		Map<String, String> graphs = new HashMap<String, String>();
		for (ElasticsearchTest test : tests) {
			for (String key : test.getProperties().keySet()) {
				if (key.contains("Img_")) {
					graphs.put((test.getUrl().substring(0, test.getUrl().length() - 9) + test.getProperties().get(key))
							.replace(" ", "%20"), (key.split("_"))[2]);
					// key: graphUrl, value: graphTitle
					// graphUrl e.g:
					// "http://0.0.0.0:9000/reports/exec_exeId/tests/test_testNumber/graph.png"
					// graphTitle e.g: Throughput graph
				}
			}
		}
		return graphs;
	}
	
	public static Map<String, String> getAllGraphsLinks(List<ElasticsearchTest> tests) {
		log.debug("calling getAllGraphsLinks method");
		Map<String, String> graphs = new HashMap<String, String>();

		for (ElasticsearchTest test : tests) {
			String images = test.getProperties().get("Images");
			// images e.g:
			// testName1,image1.png;testName2,image2.png
			if (images != null) {
				for (String image : images.split(";")) {
					// image e.g: testName1,image1.png
					String[] splits = image.split(",");
					String testName = splits[0];
					String imageName = splits[1];

					graphs.put((test.getUrl().substring(0, test.getUrl().length() - 9) + imageName).replace(" ", "%20"),
							testName);
					// key e.g:
					// "http://0.0.0.0:9000/reports/exec_exeId/tests/test_testNumber/graph.png"
					// value e.g: Throughput
				}
			}
		}
		return graphs;
	}

	public static Map<String, String> getAllDevicesVersionsFromTestsProperties(List<ElasticsearchTest> tests) {
		log.debug("calling getAllDevicesVersionsFromTestsProperties method");
		Map<String, String> devicesVersions = new LinkedHashMap<String, String>();
		for (ElasticsearchTest test : tests) {
			for (String key : test.getProperties().keySet()) {
				if (key.contains("_Version") && (key.split("_")[0].split("-").length > 1)) {
					// key e.g: SWIT9-Harmony20_Version
					devicesVersions.put(key.split("_")[0].split("-")[1], test.getProperties().get(key));
					// key = name of device
					// value = version of device
				}
			}
		}
		return devicesVersions;
	}
	
	//e.g return: Map<"Harmony67", Map<{"HW Type", "AirHarmony4000"}, {"Active Cells", "1"}, {"Band", "41"}, {"Duplex", "TDD"}, {"BW", "20MHz"}>
	public static Map<String, Map<String, String>> getAllDevicesDetails(Map<String, String> scenarioProperties) {
		if(scenarioProperties.get(RADIO_PROFILES) == null) {
			return null;
		}
		if(scenarioProperties.get(ENB_TYPE) == null || !scenarioProperties.get(ENB_TYPE).contains("Active cells")) {
			return getAllDevicesDetailsOldFormat(scenarioProperties);
		}
		Map<String, Map<String, String>> res = new LinkedHashMap<String, Map<String, String>>();
		Map<String, String> enbsType = new LinkedHashMap<String, String>();
		
		if(scenarioProperties.get(ENB_TYPE) != null) {
			for(String enb: scenarioProperties.get(ENB_TYPE).split(";")){
				//EnbType e.g: Velocity81,AirVelocity1200,Active cells: 2;Velocity82,AirVelocity1200,Active cells: 1;
				enbsType.put(enb.split(",")[0], StringUtils.substringAfter(enb, ","));
			}
		}
		
		String[] radioProfilesSplit = scenarioProperties.get(RADIO_PROFILES).split(";");
		//radioProfiles e.g: "TDD: Band41, FC2, 20MHz (Harmony67); TDD: Band41, FC2, 20MHz (Unity66)"
		
		for(String radio :radioProfilesSplit) {
			//e.g: radio = "TDD: Band41, FC2, 20MHz (Harmony67)"
			String enbName = StringUtils.substringBetween(radio, "(", ")");
			enbName = enbName.split(":")[0];
			String enbType = enbsType.get(enbName);
			//enbType e.g: "AirHarmony4000, Active cells: 1"
			Map<String, String> curr = new LinkedHashMap<String, String>();
			curr.put("HW Type", StringUtils.substringBetween(enbType, "", ","));
			String value = "NA";
			if (scenarioProperties.get(USING_DONOR_ENB) != null) {				
				value = getValueForEnodeB(enbName,scenarioProperties.get(USING_DONOR_ENB));
				value = (value.equalsIgnoreCase("true") ? "Yes" : "No");
			}
			curr.put("With Donor", value);
			
			value = "NA";
			if (scenarioProperties.get(PNP_STATUS) != null) {
				value = getValueForEnodeB(enbName,scenarioProperties.get(PNP_STATUS));
				value = (value.equalsIgnoreCase("true") ? "Yes" : "No");
			}
			curr.put("PNP", value);
			
			value = "NA";
			if (scenarioProperties.get(IPSEC_STATUS) != null) {
				value = getValueForEnodeB(enbName,scenarioProperties.get(IPSEC_STATUS));
				value = (value.equalsIgnoreCase("true") ? "Yes" : "No");
			}
			curr.put("IPSec", value);
			
			value = "NA";
			if (scenarioProperties.get(IP_VERSION) != null) {
				value = getValueForEnodeB(enbName,scenarioProperties.get(IP_VERSION));				
			}
			curr.put("IP", value);
			
			value = "NA";
			if (scenarioProperties.get(CA_CONFIGURATION) != null) {
				value = getValueForEnodeB(enbName,scenarioProperties.get(CA_CONFIGURATION));
			}
			curr.put("CA configuration", value);				
			
			curr.put("Active Cells", enbType.split(":")[1]);
			curr.put("Band", StringUtils.substringBetween(radio, "Band", ","));
			curr.put("Duplex", StringUtils.substringBetween(radio, "", ":"));
			curr.put("BW", StringUtils.substringBetween(radio.split(",")[radio.split(",").length-1], " ", "("));
			
			
			res.put(enbName, curr);
		}
		return res;
	}
	
	private static String getValueForEnodeB(String enbName, String fullString) {
		
		String[] enbStrings = fullString.split(";");
		
		String string = "";
		for (String str : enbStrings) {
			if (str.contains(enbName)) {
				string = str;
				break;
			}
		}
		
		String regex = "\\((.*)\\)";

		Matcher m = Pattern.compile(regex).matcher(string);
		if (m.find()) {
			String value = m.group(1);
			log.info("Found value '" + value + "' in String '" + fullString + "'");
			return value;
		} 
		return "NA";
	}

	//e.g return: Map<"Harmony67", Map<{"HW Type", "AirHarmony4000"}, {"Cells", "1"}, {"Band", "41"}, {"Duplex", "TDD"}, {"BW", "20MHz"}>
	public static Map<String, Map<String, String>> getAllDevicesDetailsOldFormat(Map<String, String> scenarioProperties) {
		if(scenarioProperties.get(RADIO_PROFILES) == null) {
			return null;
		}
		Map<String, Map<String, String>> res = new LinkedHashMap<String, Map<String, String>>();
		String[] radioProfilesSplit = scenarioProperties.get(RADIO_PROFILES).split(";");
		//radioProfiles e.g: "TDD: Band41, FC2, 20MHz (Harmony67); TDD: Band41, FC2, 20MHz (Unity66)"
		
		for(String radio :radioProfilesSplit) {
			//e.g: radio = "TDD: Band41, FC2, 20MHz (Harmony67)"
			String enbName = StringUtils.substringBetween(radio, "(", ")");
			String enbType = scenarioProperties.get("EnbType_"+enbName);
			//enbType e.g: "AirHarmony4000, Active cells: 1"
			Map<String, String> curr = new LinkedHashMap<String, String>();
			curr.put("HW Type", StringUtils.substringBetween(enbType, "", ","));
			curr.put("Active Cells", enbType.split(":")[1]);
			curr.put("Band", StringUtils.substringBetween(radio, "Band", ","));
			curr.put("Duplex", StringUtils.substringBetween(radio, "", ":"));
			curr.put("BW", StringUtils.substringBetween(radio.split(",")[radio.split(",").length-1], " ", "("));
			res.put(enbName, curr);
		}
		return res;
	}

	public static List<ElasticsearchTest> getAllTestsOfExecution(int executionId) throws Exception {
		log.debug("About to get all the tests of execution with id " + executionId + " from the Elastic");
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		try {
			String host = Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST);
			int port = Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT);
			try (ESClient client = new ESClient(host, port)) {
				tests = client.index(Common.ELASTIC_INDEX).document("test").search()
						.byQuery("executionId:" + executionId).asClass(ElasticsearchTest.class);

			}
			;
			// List<ElasticsearchTest> tests =
			// ESUtils.getAllByQuery(Common.ELASTIC_INDEX, "test",
			// ElasticsearchTest.class, "executionId:" + executionId);
		} catch (Throwable e) {
			log.error("get all Tests of execution with id " + executionId + " from the Elastic Failed, due to "
					+ e.getMessage());
			e.printStackTrace();
			throw new Exception("get all Tests of execution with id " + executionId
					+ " from the Elastic Failed, due to " + e.getMessage());
		}
		if (null == tests || tests.isEmpty()) {
			log.error("No tests found in execution " + executionId);
			throw new Exception("No tests found in execution " + executionId);
		}
		return tests;
	}

	public static List<ElasticsearchTest> getAllTestsOfExecutionSortedById(List<ElasticsearchTest> tests,
			int executionId) {
		Collections.sort(tests, new Comparator<ElasticsearchTest>() {
			@Override
			public int compare(ElasticsearchTest test1, ElasticsearchTest test2) {
				String[] parts1 = test1.getUid().split("-", 2);
				String[] parts2 = test2.getUid().split("-", 2);
				return (Integer.parseInt(parts1[1]) - Integer.parseInt(parts2[1]));
			}
		});
		return tests;
	}

	public static Map<String, FeatureStatus> caluculateAllStatusesPerFeaturesOfExecution(
			List<ElasticsearchTest> tests, int executionId, String targetVersion) {
		log.debug("calling caluculateAllStatusesPerFeaturesOfExecution method");
		Set<String> allFeatures = new LinkedHashSet<String>();
		Map<String, FeatureStatus> statuses = new LinkedHashMap<String, FeatureStatus>();
		for (ElasticsearchTest test : ExecutionUtils.getAllTestsOfExecutionSortedById(tests, executionId)) {
			String[] parts = test.getProperties().get("Class").split("\\.");
			if (parts.length >= 3 && parts[2] != null && !parts[2].isEmpty()) {
				allFeatures.add(parts[2]);
			}
		}

		for (String feature : allFeatures) {
			statuses.put(feature, FeatureStatus.caluculateStatusesPerFeature(tests, executionId, feature, targetVersion));
		}
		return statuses;
	}

	public static Map<String, String> getExecutionScenarioProperties(int executionId) throws Exception {
		log.debug("geting scenario properties of execution with id " + executionId + " from the Elastic");
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		Map<String, String> scenarioProperties = new HashMap<String, String>();
		try {
			tests = ExecutionUtils.getAllTestsOfExecution(executionId);
			tests = ExecutionUtils.getAllTestsOfExecutionSortedById(tests, executionId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		scenarioProperties = tests.get(tests.size() - 1).getScenarioProperties();
		if (null == scenarioProperties || scenarioProperties.isEmpty()) {
			log.warn("scenarioProperties of execution with id " + executionId + " is missing");
		}
		return scenarioProperties;
	}

	public static int findPreviousExecutionId(int executionId, String executionType) {
		log.debug("calling findPreviousExecutionId method");
		double previousExecutionId = 0;
		String host = Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST);
		int port = Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT);

		try (ESClient client = new ESClient(host, port)) {
			previousExecutionId = client.index(Common.ELASTIC_INDEX).document("test").aggs().max("executionId",
					"scenarioProperties.Type:" + executionType + " AND executionId:<" + executionId);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("findPreviousExecutionId Failed due to " + e.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
			return 0;
		}
		if (previousExecutionId <= 0 || previousExecutionId >= Integer.MAX_VALUE) {
			return 0;
		}
		return (int) previousExecutionId;
	}

	public static long getSumExecutionDuration(List<ElasticsearchTest> tests) {
		log.debug("calling getSumExecutionDuration method");
		long sum = 0;
		for (ElasticsearchTest test : tests) {
			sum += test.getDuration();
		}
		return sum;
	}
	
	public static String getStatus(ExecutionMetadata metadata, List<ElasticsearchTest> tests) {
		String status = "";
		int successTests = metadata.getNumOfSuccessfulTests();
		int warningTests = metadata.getNumOfTestsWithWarnings();
		int failTests = metadata.getNumOfFailedTests();
		int numOfAllTests = metadata.getNumOfTests();
		log.info("successTests: " + successTests);
		log.info("failTests: " + failTests);
		log.info("warningTests: " + warningTests);
		log.info("numOfAllTests: " + numOfAllTests);
		if (successTests + warningTests == numOfAllTests) {
			if ((ExecutionUtils.existsTestContainsKey(tests, "CoreFilePath_")) || (ExecutionUtils.existsTestContainsKey(tests, "CoreFiles"))){
				log.info("Execution " + metadata.getId() + " Reported as Core Warning");
				status = "Core Warning";
			}
			else{
				log.info("Execution " + metadata.getId() + " Reported as Pass");
				status = "Pass";
			}
		} else {
			log.info("Execution " + metadata.getId() + " Reported as Fail");
			status = "Fail";
		}
		return status;
	}
}