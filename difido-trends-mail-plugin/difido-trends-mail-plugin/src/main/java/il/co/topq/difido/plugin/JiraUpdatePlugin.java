package il.co.topq.difido.plugin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.plugins.ExecutionPlugin;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class JiraUpdatePlugin implements ExecutionPlugin {

	private ExecutionMetadata metadata;
	public static final String JIRA_ADDRESS = "192.168.56.47";

	public static final String SCENARIO_NAME = "Scenario";
	public static final String AUTOMATION_VERSION = "Version";
	public static final String SETUP_NAME = "SetupName";
	public static final String TARGET_VERSION = "targetVersion";
	public static final String DURATION = "duration";
	public static final String START_TIME_DATE = "Date";
	public static final String VERSION_BRANCH = "Branch";
	public static final String DIFIDO_LINK = "url";
	// Setup Configuration Table
	public static final String ENB_TYPE = "EnbType";
	public static final String RELAY_VERSION = "RelayVersions";
	public static final String NETSPAN_VERSION = "NetspanVar";

	private static final Logger log = LoggerFactory.getLogger(JiraUpdatePlugin.class);

	private Configuration config = null;

	private static final String PROJECT_ID = "project_id";
	private static final String TEST_ISSUE_TYPE_ID = "test_issue_type_id";

	private static final String BRANCH_ID = "branch_field_id";
	private static final String BUILD_ID = "build_field_id";
	private static final String ENODEB_TYPE_ID = "enodeb_type_field_id";
	private static final String TEST_LINK_ID = "test_link_field_id";
	private static final String DURATION_ID = "duration_field_id";
	private static final String SETUP_NAME_ID = "setup_field_id";
	private static final String REASON_ID = "reason_field_id";
	private static final String START_TIME_ID = "start_time_field_id";
	private static final String TEST_TYPE_ID = "test_type_id";
	private static final String SCENARIO_NAME_ID = "scenario_name_field_id";
	private static final String AUTOMATION_VERSION_ID = "automation_version_field_id";

	private String rawEnbTypeString = null;
	
	public static void main(String[] args){
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
	public String getName() {
		return "JiraUpdatePlugin";
	}

	@Override
	public void onExecutionEnded(ExecutionMetadata incomingMetaData) {
		log.info("onExecutionEnded in " + getName());
		this.metadata = incomingMetaData;
		try {
			// We need to give time to the Elastic to index the data
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			log.error("Thread.sleep Failed due to " + e1.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
		}

		if (null == metadata) {
			log.error("Metadata object is null");
			return;
		}
		config = Configuration.getInstance(Configuration.JIRA_CONFIG_FILE_NAME);

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
			String testKey = checkTestKeyIssueExists(test);
			if (testKey == "") {
				log.warn("test '" + test.getName() + "' key - " + testKey + " does not exist in jira!");
				continue;
			}
			
			res &= createJiraIssue(exeKey, testKey, test);
		}
		return res;
	}

	private String createExecution(List<ElasticsearchTest> allTests, Map<String, String> scenarioProperties) {
		String scenarioName = scenarioProperties.get(SCENARIO_NAME);
		
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
					String enbType = rawEnbType.split("\\(")[0].trim();
					log.debug("enbType: " + enbType);
					String enbName = rawEnbType.split("\\(")[1].replaceAll("\\)", "").trim();
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
		String duration = allTests.get(0).getExecutionDuration() + "";
		log.debug("duration is " + duration);
		String branch = scenarioProperties.get(VERSION_BRANCH);
		log.debug("branch is " + branch);
		
		String projectId = config.readString(PROJECT_ID);
		String enodebTypeId = config.readString(ENODEB_TYPE_ID);
		String durationId = config.readString(DURATION_ID);
		String startTimeId = config.readString(START_TIME_ID);
		String buildId = config.readString(BUILD_ID);
		String branchId = config.readString(BRANCH_ID);
		String automationVersionId = config.readString(AUTOMATION_VERSION_ID);
		String setupId = config.readString(SETUP_NAME_ID);
		String linkId = config.readString(TEST_LINK_ID);
		String scenarioNameid = config.readString(SCENARIO_NAME_ID);
		
		String bodyString = "{\"fields\": { \"project\":{\"id\": \"" + projectId + "\"},"
		+ "\"summary\": \"" + executionName + "\","
		+ "\"customfield_"+durationId+"\": "+duration+"," // Duration (units?)
		+ "\"customfield_"+buildId+"\": "+swBuild+"," // target build
		+ "\"customfield_"+branchId+"\": [\""+branch+"\"]," // branch
		+ "\"customfield_"+automationVersionId+"\": [\""+automationVersion+"\"]," // automation version
		+ "\"customfield_"+enodebTypeId+"\": [ "+enbTypesString+"]," // Dut types
		+ "\"customfield_"+setupId+"\": [\""+setupName+"\"]," // setup name
		+ "\"customfield_"+linkId+"\": \""+indexLink+"\"," // link to test
		+ "\"customfield_"+scenarioNameid+"\": [\""+scenarioName+"\"]," // scenario name
		+ "\"customfield_"+startTimeId+"\": \""+startTime+"\"," // begin time 2017-11-19T13:09:29.0+0200
		+ " \"fixVersions\": [{\"name\": \"" + swVersion + "\"}],"
		+ " \"issuetype\": {\"name\": \"Test Execution\"}}}";
		String url = "http://" + JIRA_ADDRESS + ":8080/rest/api/2/issue/";

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
		String bodyString = "{\"name\":\"" + swVersion + "\","
						+ "\"project\":\"SWIT\","
						+ "\"released\": false}";
		String url = "http://" + JIRA_ADDRESS + ":8080/rest/api/2/version/";

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
	
//	private String getComponent(String scenarioName) {
//		log.debug("scenarioName: " + scenarioName);
//		List<String> allComponenets = config.readList(SCENARIO_COMPOENENTS);
//		for (String scenarioCompoenent : allComponenets) {
//			log.debug("Componenet: " + scenarioCompoenent);
//			List<String> scenarios = config.readList(scenarioCompoenent);
//			for (String scenario : scenarios) {
//				log.debug("scenario: " + scenario);
//				if (scenario.equals(scenarioName)) {
//					return scenarioCompoenent;
//				}
//			}
//		}
//		return "None";
//	}

	public boolean createJiraIssue( String executionKey, String testKey, ElasticsearchTest test) {
		
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
		int durationSec = (int) (test.getDuration() / 1000);
		log.debug("durationSec: " + durationSec);
		String duration = durationSec + "";
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
					String enbType = rawEnbType.split("\\(")[0].trim();
					log.debug("enbType: " + enbType);
					String enbName = rawEnbType.split("\\(")[1].replaceAll("\\)", "").trim();
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
		
		try {
			String projectId = config.readString(PROJECT_ID);
			String issueTypeId = config.readString(TEST_ISSUE_TYPE_ID);
			String enodebTypeId = config.readString(ENODEB_TYPE_ID);
			String testLinkId = config.readString(TEST_LINK_ID);
			String durationId = config.readString(DURATION_ID);
			String reasonId = config.readString(REASON_ID);
			String startTimeId = config.readString(START_TIME_ID);
			String testTypeId = config.readString(TEST_TYPE_ID);
			String responseText = "";
			log.debug("projectid from configfile: " + projectId);
			
			String bodyString = "{\"fields\": {\"project\":{\"id\": \""+projectId+"\"},"
					+ "\"summary\": \""+summary+"\",\"issuetype\": {\"id\": \""+issueTypeId+"\"},"
					+ "\"customfield_"+testTypeId+"\": { \"value\": \"Automation\" },"
					+ "\"customfield_"+reasonId+"\": \""+reason+"\","
					+ "\"customfield_"+durationId+"\": "+duration+" ,"
					+ "\"customfield_"+testLinkId+"\": \""+link+"\","
					+ "\"customfield_"+startTimeId+"\": \""+startTime+"\","
					+ "\"customfield_"+enodebTypeId+"\":  [ "+enbTypesString+"]}}";
			
			String url = "http://"+JIRA_ADDRESS+":8080/rest/api/2/issue/";
			
			responseText = makeRestPostRequest(bodyString, url);
			
			if(!responseText.equals("-999"))
			{
				String regex = "\"key\":\"(.+)\",";

				Matcher m = Pattern.compile(regex).matcher(responseText);

				if (m.find()) {
					String issueKey = m.group(1);
					log.info("test issue " + summary + " created successfully with key" + issueKey);
					if(!linkIssue(issueKey, testKey))
					{
						log.warn("Failed linked test " + issueKey);
						return false;
					}
					if(!addTestToExecution(executionKey, issueKey, result))
					{
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
		String url = "http://"+JIRA_ADDRESS+":8080/rest/raven/1.0/api/testexec/" + executionKey + "/test";
		
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
		String url = "http://"+JIRA_ADDRESS+":8080/rest/raven/1.0/api/testrun/" + runId + "/status?status=" + result;
		
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
			return "Warning";
		default:
			return "ABORTED";
		}
	}

	private int getTestRunId(String executionKey, String issueKey) {
		log.info("getTestRunId: " + issueKey + ", " + executionKey);
		String url = "http://"+JIRA_ADDRESS+":8080/rest/raven/1.0/api/testrun?testExecIssueKey=" + executionKey + "&testIssueKey=" + issueKey;
		
		String ans = makeRestGetRequest(url);
		
		if (ans.equals("-999")) {
			log.warn("failed getTestRunId");
			return -999;
		}
		int testRunId = JsonPath.read(ans, "$.id");
		
		return testRunId;
	}


	private boolean linkIssue(String issueKey, String testKey) {
		log.info("linkIssue: " + issueKey + ", " + testKey);
	
		String bodyString = "{\"type\": {\"name\": \"Execute\"},"
				+ "\"inwardIssue\": {\"key\": \"" + testKey + "\"},"
				+ "\"outwardIssue\": {\"key\": \"" + issueKey + "\"}}";
		
		String url = "http://"+JIRA_ADDRESS+":8080/rest/api/2/issueLink";
		
		String ans = makeRestPostRequest(bodyString, url);
		
		return !ans.equals("-999");
	}
	
	private String checkTestKeyIssueExists(ElasticsearchTest test) {
		String ans = "SWIT-240";
		String testName = test.getName();
		if (testName.contains("Software")) {
			ans = "SWIT-17";
		}
		if (testName.contains("1024")) {
			ans = "SWIT-19";
		}
		if (testName.contains("1400")) {
			ans = "SWIT-18";
		}
		if (testName.contains("Multiple")) {
			ans = "SWIT-22";
		}
		
		return ans;
	}	

	private String makeRestPostRequest(String bodyString, String url) {
		log.debug("Jira res request:");
		log.debug("URL: " + url);
		log.debug("body: " + bodyString);
		OkHttpClient client = new OkHttpClient();
		String responseText = "";
		boolean result = false;
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, bodyString);
		Request request = new Request.Builder().url(url).post(body).addHeader("content-type", "application/json")
				.addHeader("authorization", "Basic b2luZ2JlcjoxMjM0").addHeader("cache-control", "no-cache")
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
	
	private String makeRestGetRequest(String url) {
		log.debug("Jira res request:");
		log.debug("URL: " + url);
		OkHttpClient client = new OkHttpClient();
		String responseText = "";
		boolean result = false;
		Request request = new Request.Builder().url(url).get().addHeader("content-type", "application/json")
				.addHeader("authorization", "Basic b2luZ2JlcjoxMjM0").addHeader("cache-control", "no-cache")
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
				.addHeader("authorization", "Basic b2luZ2JlcjoxMjM0").addHeader("cache-control", "no-cache")
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
}
