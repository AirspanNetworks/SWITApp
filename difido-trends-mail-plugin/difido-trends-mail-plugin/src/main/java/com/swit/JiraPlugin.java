package com.swit;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.plugins.ExecutionPlugin;
import il.co.topq.report.plugins.InteractivePlugin;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class JiraPlugin implements ExecutionPlugin, InteractivePlugin {

	public enum IssueTypes {
		Test("Test"), Test_Execution("Test_Execution"), Sub_Test("Sub_Test"), Sub_Test_Execution("Sub_Test_Execution");

		private String value;

		IssueTypes(String v) {
			value = v;
		}

		public String value() {
			return value;
		}
	}

	private ExecutionMetadata metadata;
	public static String JIRA_SERVER_ADDRESS = "jira_server_address";
	public static String PROJECT_NAME = "SAR";
	// public static String JIRA_ADDRESS = "192.168.58.250";
	public static String SCENARIO_NAME = "Scenario";
	public static String MAILING_LIST = "MailingList";
	public static String AUTOMATION_VERSION = "Version";
	public static String SETUP_NAME = "SetupName";
	public static String TARGET_VERSION = "targetVersion";
	public static String DURATION = "duration";
	public static String START_TIME_DATE = "Date";
	public static String VERSION_BRANCH = "Branch";
	public static String DIFIDO_LINK = "url";
	// Setup Configuration Table
	public static String ENB_TYPE = "EnbType";
	public static String RELAY_VERSION = "RelayVersions";
	public static String NETSPAN_VERSION = "NetspanVer";

	private static Logger log = LoggerFactory.getLogger(JiraPlugin.class);

	private Configuration config = null;
	private static String jiraServer;
	private static String PROJECT_ID = "project_id";
	private static String TEST_ISSUE_TYPE_ID = "test_issue_type_id";

	private static String BRANCH_ID = "branch_field_id";
	private static String BUILD_ID = "build_field_id";
	private static String OFFICIAL_ID = "official_field_id";
	private static String ENODEB_TYPE_ID = "enodeb_type_field_id";
	private static String ENODEB_NAME_ID = "enodeb_name_field_id";
	private static String TEST_LINK_ID = "test_link_field_id";
	private static String DURATION_ID = "duration_field_id";
	private static String SETUP_NAME_ID = "setup_field_id";
	private static String TEST_ENV_ID = "test_env_field_id";
	private static String REASON_ID = "reason_field_id";
	private static String START_TIME_ID = "start_time_field_id";
	private static String TEST_TYPE_ID = "test_type_field_id";
	private static String SCENARIO_NAME_ID = "scenario_name_field_id";
	private static String AUTOMATION_VERSION_ID = "automation_version_filed_id";
	private static String RELAY_VERSION_ID = "relay_version_field_id";

	private String rawEnbTypeString = null;
	private String basicUrl = null;

	public static void main(String[] args) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dt = null;

		try {
			dt = formatter.parse("2017/10/19 08:48:32");
		} catch (ParseException e1) {
			log.warn("Failed parsing start time.");
			e1.printStackTrace();
		}

		System.out.println("ZoneId: " + TimeZone.getDefault());
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.0Z");
		System.out.println("formatted date: " + formatter1.format(dt));
	}

	@Override
	public void execute(List<ExecutionMetadata> metaDataList, String params) {
		System.out.println("execute");
		try {
			log.info("Params: " + params);
			if (params.equals("1234"))
				onExecutionEnded(metaDataList.get(0));
			else
				log.warn("Wrong password!");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String executeInteractively(List<ExecutionMetadata> metaDataList, String params) {
		execute(metaDataList, params);
		return "<p>Success</p>";
	}

	@Override
	public String getName() {
		return "JiraPlugin";
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata incomingMetaData) {
		log.info("onExecutionEnded in " + getName());
		this.metadata = incomingMetaData;
		try {
			// We need to give time to the Elastic to index the data
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			log.error("Thread.sleep Failed due to " + e1.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
		}

		if (null == metadata) {
			log.error("Metadata object is null");
			return;
		}
		config = Configuration.getInstance(Configuration.JIRA_CONFIG_FILE_NAME);
		jiraServer = config.readString(JIRA_SERVER_ADDRESS);
		basicUrl = "http://" + jiraServer + ":8080/";
		if (!updateUrl())
			return;
		int executionId = metadata.getId();
		List<ElasticsearchTest> allTests;
		List<ElasticsearchTest> sortedTests;
		Map<String, String> scenarioProperties;
		try {
			allTests = ExecutionUtils.getAllTestsOfExecution(executionId);
			sortedTests = ExecutionUtils.getAllTestsOfExecutionSortedById(allTests, executionId);
			scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(executionId);
		} catch (Exception e) {
			log.error("Exception due to: " + e.getMessage());
			return;
		}
		log.debug("scenarioProperties:");
		for (String key : scenarioProperties.keySet()) {
			log.debug("Key: " + key + ",  value: " + scenarioProperties.get(key));
		}
		jiraInteraction(sortedTests, scenarioProperties);

	}

	private boolean jiraInteraction(List<ElasticsearchTest> allTests, Map<String, String> scenarioProperties) {
		String exeKey = createExecution(allTests, scenarioProperties);
		boolean res = true;
		if (exeKey == "") {
			log.warn("Failed creating execution");
			return false;
		}

		for (ElasticsearchTest test : allTests) {
			long testDuratrion = test.getDuration();
			if (testDuratrion == 0) {
				log.warn("test '" + test.getName() + "' duration is 0, incomplete test, not added to jira!");
				continue;
			}
			/*
			 * String testKey = checkTestKeyIssueExists(test); if (testKey ==
			 * "") { log.warn("test '" + test.getName() + "' key - " + testKey +
			 * " does not exist in jira!"); continue; }
			 */

			res &= createJiraIssue(exeKey, test);
		}
		return res;
	}

	private String createExecution(List<ElasticsearchTest> allTests, Map<String, String> scenarioProperties) {
		String scenarioName = scenarioProperties.get(SCENARIO_NAME);
		String mailingList = scenarioProperties.get(MAILING_LIST);
		String setupName = scenarioProperties.get(SETUP_NAME);
		log.debug("setupName is " + setupName);

		String uri = metadata.getUri();
		String urlFirstTest = allTests.get(0).getUrl();
		String indexLink = urlFirstTest.split("/")[0] + "//" + urlFirstTest.split("/")[2] + "/" + uri;
		log.debug("url is " + indexLink);

		String startTime = allTests.get(0).getExecutionTimeStamp();

		log.debug("startTime is " + startTime);
		String executionName = scenarioName + "_" + setupName + "_" + startTime;

		startTime = convertToJiraTime(startTime);

		List<String> enbNames = new ArrayList<String>();
		List<String> enbTypes = new ArrayList<String>();
		rawEnbTypeString = scenarioProperties.get(ENB_TYPE);
		if (rawEnbTypeString != null) {
			log.debug("rawEnbTypeString: " + rawEnbTypeString);
			String[] rawEnbTypes = rawEnbTypeString.split(";");
			Map<String, String> devices = ExecutionUtils.getAllDevicesVersionsFromTestsProperties(allTests);
			for (String deviceName : devices.keySet()) {
				log.debug("deviceName: " + deviceName);
				for (String rawEnbType : rawEnbTypes) {
					log.debug("rawEnbType: " + rawEnbType);
					String enbType = rawEnbType.split(",")[1].trim();
					log.debug("enbType: " + enbType);
					String enbName = rawEnbType.split(",")[0].trim();
					log.debug("enbName: " + enbName);
					if (deviceName.contains(enbName)) {
						log.debug("Adding latest device.");
						enbNames.add(enbName);
						enbTypes.add(enbType);
					}
				}
			}
		}

		String enbTypesString = "";
		for (String enbType : enbTypes) {
			enbTypesString += "\"" + enbType + "\",";
		}
		if (enbTypesString.length() > 0) {
			enbTypesString = enbTypesString.substring(0, enbTypesString.length() - 1);
		}
		log.debug("enbTypesString: " + enbTypesString);

		String swVersion = "-999";
		String swBuild = "-999";
		String swRelease = "-999";
		try {
			swVersion = scenarioProperties.get(TARGET_VERSION);
			log.debug("software version: " + swVersion);
			String[] octatArray = swVersion.split("\\.");
			log.debug("software version has " + octatArray.length + "parts");
			if (octatArray.length >= 1) {
				swBuild = octatArray[octatArray.length - 1];
				log.debug("software build is " + swBuild);
			}
			if (octatArray.length >= 2) {
				swRelease = octatArray[0] + "." + octatArray[1];
				log.debug("software release is " + swRelease);
			}

		} catch (Exception e) {
			log.warn("Failed getting SW version");
			swBuild = "-999";
			swRelease = "-999";
		}
		addVersionToJira(swVersion);

		String automationVersion = scenarioProperties.get(AUTOMATION_VERSION);
		log.debug("automationVersion is " + automationVersion);
		String duration = millis2time(metadata.getDuration()) + "";
		log.debug("duration is " + duration);
		String branch = scenarioProperties.get(VERSION_BRANCH);
		log.debug("branch is " + branch);

		String official = ExecutionUtils.getStatus(metadata, allTests).equals("Pass") ? "Yes" : "No";
		String relayVersion = scenarioProperties.get(RELAY_VERSION);
		if (relayVersion == null)
			relayVersion = "";
		String projectId = config.readString(PROJECT_ID);
		String enodebTypeId = config.readString(ENODEB_TYPE_ID);
		String durationId = config.readString(DURATION_ID);
		String relayVersionId = config.readString(RELAY_VERSION_ID);
		String startTimeId = config.readString(START_TIME_ID);
		String component = getComponent(scenarioName, mailingList);
		String buildId = config.readString(BUILD_ID);
		String officialId = config.readString(OFFICIAL_ID);
		String branchId = config.readString(BRANCH_ID);
		String automationVersionId = config.readString(AUTOMATION_VERSION_ID);
		String setupId = config.readString(SETUP_NAME_ID);
		String testEnvId = config.readString(TEST_ENV_ID);
		String linkId = config.readString(TEST_LINK_ID);
		String scenarioNameid = config.readString(SCENARIO_NAME_ID);

		String bodyString = "{\"fields\": { \"project\":{\"id\": \"" + projectId + "\"}," + "\"summary\": \""
				+ executionName + "\"," + "\"components\": [{\"name\": \"" + component + "\"}]," + "\"customfield_"
				+ durationId + "\": \"" + duration + "\"," // Duration(units?)
				+ "\"customfield_" + buildId + "\": " + swBuild + "," // target_build
				+ "\"customfield_" + branchId + "\": [\"" + branch + "\"]," // branch
				+ "\"customfield_" + automationVersionId + "\": [\"" + automationVersion + "\"]," // automation_version
				+ "\"customfield_" + enodebTypeId + "\": [ " + enbTypesString + "]," // Dut_types
				+ "\"customfield_" + setupId + "\": [\"" + setupName + "\"]," // setup_name
				+ "\"customfield_" + officialId + "\": {\"value\": \"" + official + "\"}," + "\"customfield_"
				+ testEnvId + "\": [\"" + setupName + "\"]," // test_env
				+ "\"customfield_" + linkId + "\": \"" + indexLink + "\"," // link_to_test
				+ "\"customfield_" + relayVersionId + "\": \"" + relayVersion + "\"," // relay_version
				+ "\"customfield_" + scenarioNameid + "\": [\"" + scenarioName + "\"]," // scenario_name
				+ "\"customfield_" + startTimeId + "\": \"" + startTime + "\"," // begin_time_2017-11-19T13:09:29.0+0200
				+ " \"fixVersions\": [{\"name\": \"" + swVersion + "\"}],"
				+ " \"issuetype\": {\"name\": \"Test Execution\"}}}";
		String url = basicUrl + "rest/api/2/issue/";

		String responseText = makeRestPostRequest(bodyString, url);

		String regex = "\"key\":\"(.+)\",";

		Matcher m = Pattern.compile(regex).matcher(responseText);

		if (m.find()) {
			String executionKey = m.group(1);
			log.info("Successfully created execution - key: " + executionKey + ", name: " + executionName);
			return executionKey;
		} else {
			log.warn("Failed to get issue key, no regex match");
			log.warn("regex: " + regex);
			log.warn("response string: " + responseText);
		}

		return "";
	}

	private void addVersionToJira(String swVersion) {
		String bodyString = "{\"name\":\"" + swVersion + "\"," + "\"project\":\"" + PROJECT_NAME + "\","
				+ "\"released\": false}";
		String url = basicUrl + "rest/api/2/version";

		makeRestPostRequest(bodyString, url);
	}

	private String convertToJiraTime(String startTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dt = null;

		try {
			dt = formatter.parse(startTime);
		} catch (ParseException e1) {
			log.warn("Failed parsing start time.");
			e1.printStackTrace();
		}
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.0Z");
		startTime = formatter1.format(dt);
		log.debug("formatted date: " + startTime);
		return startTime;
	}

	private String getComponent(String scenarioName, String mailingList) {
		log.info("scenarioName: " + scenarioName + ", mailingList: " + mailingList);
		if (mailingList == null || mailingList.toLowerCase().contains("debug") || mailingList.equals(""))
			return "Debug";
		if (scenarioName.toLowerCase().contains("reg")) {
			if (scenarioName.toLowerCase().contains("p0")) {
				return "Regression P0";
			} else if (scenarioName.toLowerCase().contains("p1")) {
				return "Regression P1";
			}
		} else if (scenarioName.toLowerCase().contains("sanity"))
			return "Sanity";
		else if (scenarioName.toLowerCase().contains("stability"))
			return "Stability";
		else if (scenarioName.toLowerCase().contains("smoke"))
			return "Smoke";
		return "None";
	}

	public boolean createJiraIssue(String executionKey, ElasticsearchTest test) {

		String summary = test.getName();
		String reason = test.getProperties().get("failureReason");
		if (reason == null) {
			reason = "";
		}
		reason = reason.replace("\n", "");
		log.info("Creating jira test issue: " + summary);
		String result = test.getStatus();
		String startTime = test.getExecutionTimeStamp();
		log.debug("startTime is " + startTime);
		startTime = convertToJiraTime(startTime);
		String duration = millis2time(test.getDuration());
		log.debug("duration: " + duration);
		String link = test.getUrl();

		List<String> enbNames = new ArrayList<String>();
		List<String> enbTypes = new ArrayList<String>();
		List<ElasticsearchTest> singleTest = new ArrayList<ElasticsearchTest>();
		singleTest.add(test);

		if (rawEnbTypeString != null) {
			log.debug("rawEnbTypeString: " + rawEnbTypeString);
			String[] rawEnbTypes = rawEnbTypeString.split(";");
			Map<String, String> devices = ExecutionUtils.getAllDevicesVersionsFromTestsProperties(singleTest);
			for (String deviceName : devices.keySet()) {
				log.debug("deviceName: " + deviceName);
				for (String rawEnbType : rawEnbTypes) {
					log.debug("rawEnbType: " + rawEnbType);
					String enbType = rawEnbType.split(",")[1].trim();
					log.debug("enbType: " + enbType);
					String enbName = rawEnbType.split(",")[0].trim();
					log.debug("enbName: " + enbName);
					if (deviceName.contains(enbName)) {
						log.debug("Adding latest device.");
						enbNames.add(enbName);
						enbTypes.add(enbType);
					}
				}
			}
		}
		String enbNamesString = "";
		for (String enbType : enbTypes) {
			enbNamesString += "\"" + enbType + "\",";
		}
		if (enbNamesString.length() > 0) {
			enbNamesString = enbNamesString.substring(0, enbNamesString.length() - 1);
		}
		log.debug("enbTypesString: " + enbNamesString);

		try {
			String projectId = config.readString(PROJECT_ID);
			String issueTypeId = config.readString(TEST_ISSUE_TYPE_ID);
			String enodebNameId = config.readString(ENODEB_NAME_ID);
			String testLinkId = config.readString(TEST_LINK_ID);
			String durationId = config.readString(DURATION_ID);
			String reasonId = config.readString(REASON_ID);
			String startTimeId = config.readString(START_TIME_ID);
			String testTypeId = config.readString(TEST_TYPE_ID);
			String responseText = "";
			log.debug("projectid from configfile: " + projectId);

			String bodyString = "{\"fields\": {\"project\":{\"id\": \"" + projectId + "\"}," + "\"summary\": \""
					+ summary + "\",\"issuetype\": {\"id\": \"" + issueTypeId + "\"}," + "\"customfield_" + testTypeId
					+ "\": { \"value\": \"Automation\" }," + "\"customfield_" + reasonId + "\": \"" + reason + "\","
					+ "\"customfield_" + durationId + "\": \"" + duration + "\" ," + "\"customfield_" + testLinkId
					+ "\": \"" + link + "\"," + /*"\"customfield_" + startTimeId + "\": \"" + startTime + "\","
					+ */"\"customfield_" + enodebNameId + "\":  [ " + enbNamesString + "]}}";

			String url = basicUrl + "/rest/api/2/issue/";

			responseText = makeRestPostRequest(bodyString, url);

			if (!responseText.equals("-999")) {
				String regex = "\"key\":\"(.+)\",";

				Matcher m = Pattern.compile(regex).matcher(responseText);

				if (m.find()) {
					String issueKey = m.group(1);
					/*
					 * log.info("test issue " + summary +
					 * " created successfully with key" + issueKey); if
					 * (!linkIssue(issueKey, testKey)) { log.warn(
					 * "Failed linked test " + issueKey); return false; }
					 */
					if (!addTestToExecution(executionKey, issueKey, result)) {
						log.warn("Failed Adding test " + issueKey);
						return false;
					}
				} else {
					log.warn("Failed to get issue key, no regex match");
					log.warn("regex: " + regex);
					log.warn("response string: " + responseText);
					return false;
				}
				return true;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean addTestToExecution(String executionKey, String issueKey, String result) {
		log.info("addTestToExecution: " + executionKey + ", " + issueKey);
		String bodyString = "{\"add\": [\"" + issueKey + "\"]}";
		String url = basicUrl + "rest/raven/1.0/api/testexec/" + executionKey + "/test";

		String ans = makeRestPostRequest(bodyString, url);

		if (!ans.equals("-999")) {
			return updateTestRunStatus(executionKey, issueKey, result);
		}
		log.warn("failed addTestToExecution");
		return false;

	}

	private boolean updateTestRunStatus(String executionKey, String issueKey, String result) {
		log.info("updateTestRunStatus: " + issueKey + ", " + executionKey + ", " + result);
		int runId = getTestRunId(executionKey, issueKey);
		if (runId == -999) {
			return false;
		}
		result = translateResult(result);
		String url = basicUrl + "rest/raven/1.0/api/testrun/" + runId + "/status?status=" + result;

		String ans = makeRestPutRequest(url);

		return !ans.equals("-999");
	}

	private String translateResult(String result) {
		switch (result) {
		case "failure":
			return "Fail";
		case "success":
			return "Pass";
		case "warning":
			return "Pass";
		default:
			return "ABORTED";
		}
	}

	private int getTestRunId(String executionKey, String issueKey) {
		log.info("getTestRunId: " + issueKey + ", " + executionKey);
		String url = basicUrl + "rest/raven/1.0/api/testrun?testExecIssueKey=" + executionKey + "&testIssueKey="
				+ issueKey;

		String ans = makeRestGetRequest(url);

		if (ans.equals("-999")) {
			log.warn("failed getTestRunId");
			return -999;
		}
		int testRunId = JsonPath.read(ans, "$.id");

		return testRunId;
	}

	/*
	 * private boolean linkIssue(String issueKey, String testKey) { log.info(
	 * "linkIssue: " + issueKey + ", " + testKey);
	 * 
	 * String bodyString = "{\"type\": {\"name\": \"Execute\"}," +
	 * "\"inwardIssue\": {\"key\": \"" + testKey + "\"}," +
	 * "\"outwardIssue\": {\"key\": \"" + issueKey + "\"}}";
	 * 
	 * String url =rest/api/2/issueLink";
	 * 
	 * String ans = makeRestPostRequest(bodyString, url);
	 * 
	 * return !ans.equals("-999"); }
	 * 
	 * private String checkTestKeyIssueExists(ElasticsearchTest test) { String
	 * ans = "SAR-20";
	 * 
	 * String testName = test.getName(); if (testName.contains("Software")) {
	 * ans = "SWIT-17"; } if (testName.contains("1024")) { ans = "SWIT-19"; } if
	 * (testName.contains("1400")) { ans = "SWIT-18"; } if
	 * (testName.contains("Multiple")) { ans = "SWIT-22"; }
	 * 
	 * 
	 * return ans; }
	 */

	private String makeRestPostRequest(String bodyString, String url) {
		log.debug("Jira res request:");
		log.debug("URL: " + url);
		log.debug("body: " + bodyString);
		int attempt = 1;
		String responseText = "";
		ResponseBody respBody = null;
		Response response = null;
		while (attempt < 4) {
			OkHttpClient client = new OkHttpClient(); // getHttpClient(url);
			boolean result = false;
			MediaType mediaType = MediaType.parse("application/json");
			RequestBody body = RequestBody.create(mediaType, bodyString);
			Request request = new Request.Builder().url(url).post(body).addHeader("content-type", "application/json")
					.addHeader("authorization", "Basic c3dpdF9hdXRvOnN3aXRfYXV0bw==")
					.addHeader("cache-control", "no-cache")
					.addHeader("postman-token", "af565a4f-150d-3871-831a-901a360e68a5").build();
			try {
				response = client.newCall(request).execute();
				result = response.isSuccessful();
				if (result) {
					respBody = response.body();
					responseText = respBody.string().replace("\n", "");
					log.debug("responseText: " + responseText);
					respBody.close();
					break;
				} else {
					log.info("Post request faild in the " + attempt + " attempt.");
					responseText = "-999";
					attempt++;
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					if (respBody != null){
						response.close(); 
						respBody.close();
					}
					log.info("Post request faild in the " + attempt + " attempt.");
					responseText = "-999";
					attempt++;
					Thread.sleep(5000);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				continue;
			}
			finally {
				if (respBody != null){
					response.close(); 
					respBody.close();
				}
			}
		}
		return responseText;
	}

	private String makeRestGetRequest(String url) {
		log.debug("Jira res request:");
		log.debug("URL: " + url);
		OkHttpClient client = new OkHttpClient();
		String responseText = "";
		boolean result = false;
		Request request = new Request.Builder().url(url).get().addHeader("content-type", "application/json")
				.addHeader("authorization", "Basic c3dpdF9hdXRvOnN3aXRfYXV0bw==").addHeader("cache-control", "no-cache")
				.addHeader("postman-token", "af565a4f-150d-3871-831a-901a360e68a5").build();
		try {
			Response response = client.newCall(request).execute();
			result = response.isSuccessful();
			ResponseBody respBody = response.body();
			responseText = respBody.string().replace("\n", "");
			log.debug("responseText: " + responseText);
			respBody.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!result) {
			return "-999";
		}
		return responseText;
	}

	private String makeRestPutRequest(String url) {
		log.debug("Jira res request:");
		log.debug("URL: " + url);
		OkHttpClient client = new OkHttpClient();
		String responseText = "";
		boolean result = false;
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "");
		Request request = new Request.Builder().url(url).put(body).addHeader("content-type", "application/json")
				.addHeader("authorization", "Basic c3dpdF9hdXRvOnN3aXRfYXV0bw==").addHeader("cache-control", "no-cache")
				.addHeader("postman-token", "af565a4f-150d-3871-831a-901a360e68a5").build();
		try {
			Response response = client.newCall(request).execute();
			result = response.isSuccessful();
			ResponseBody respBody = response.body();
			responseText = respBody.string().replace("\n", "");
			log.debug("responseText: " + responseText);
			respBody.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!result) {
			return "-999";
		}
		return responseText;
	}

	private boolean updateUrl() {
		String url = basicUrl + "rest/api/2/issue/createmeta";
		String res = makeRestGetRequest(url);
		if (res == "-999") {
			log.info("URL \"" + url + "\" is not responding.\nTrying https");
			basicUrl = "https://" + jiraServer + "/";
			url = basicUrl + "rest/api/2/issue/createmeta";
			res = makeRestGetRequest(url);
			if (res == "-999") {
				log.info("URL \"" + url + "\" is not responding as well.\nStoping report");
				return false;
			}
		}
		return true;
	}

	private String millis2time(long durationInMillis) {
		long second = (durationInMillis / 1000) % 60;
		long minute = (durationInMillis / (1000 * 60)) % 60;
		long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

		String time = String.format("%02d:%02d:%02d", hour, minute, second);
		return time;
	}

	private OkHttpClient getHttpClient(String url) {
		if (url.contains("https")) {
			SSLContext sslContext;
			TrustManager[] trustManagers;
			try {
				KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				keyStore.load(null, null);
				InputStream certInputStream = new FileInputStream("/home/difido/plugin/cert.pem");
				BufferedInputStream bis = new BufferedInputStream(certInputStream);
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				while (bis.available() > 0) {
					Certificate cert = certificateFactory.generateCertificate(bis);
					keyStore.setCertificateEntry("https://helpdesk.airspan.com", cert);
				}
				TrustManagerFactory trustManagerFactory = TrustManagerFactory
						.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(keyStore);
				trustManagers = trustManagerFactory.getTrustManagers();
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, trustManagers, null);
			} catch (Exception e) {
				e.printStackTrace(); // TODO replace with real exception handling tailored to your needs
				return null;
			}

			OkHttpClient client = new OkHttpClient.Builder()
					.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0]).build();
			return client;
		} else
			return new OkHttpClient();
	}
}
