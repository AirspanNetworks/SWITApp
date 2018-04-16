package il.co.topq.difido.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class TestTemplates {
	private Templates templates;
	
	@Before
	public void setUp() {
		templates = new Templates();
	}

	@Test
	public void testStatusTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		final ExecutionStatus currentExecutionStatus = new ExecutionStatus(100, "Sanity", "1.0.1", 11, 2, 3, "0:07:50");
		String body = templates.populateStatusTemplate("execution description", currentExecutionStatus);
		Assert.assertNotNull(body);
		Assert.assertTrue(body.contains("11"));
		Assert.assertTrue(body.contains("2"));
		Assert.assertTrue(body.contains("3"));
	}
	
	@Test
	public void testStatusesPerFeatureTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		ElasticsearchTest test1 = new ElasticsearchTest();
		ElasticsearchTest test2 = new ElasticsearchTest();
		tests.add(test1);
		tests.add(test2);
		Map<String, FeatureStatus> statuses = new LinkedHashMap<String, FeatureStatus>();
		final FeatureStatus currentExecutionStatus1 = new FeatureStatus(100, "TPT", "1.1.1", 1, 0, 0, "00:7:50");
		final FeatureStatus currentExecutionStatus2 = new FeatureStatus(100, "HO", "1.1.1", 1, 0, 0, "00:7:50");
		final ExecutionStatus all = new ExecutionStatus(100, "type", "1.1.1", 2, 0, 0, "0:15:40");
		statuses.put("TPT", currentExecutionStatus1);
		statuses.put("HO", currentExecutionStatus2);
		String body = templates.populateStatuesPerFearuresTemplate(all, statuses);
		Assert.assertNotNull(body);
		Assert.assertTrue(body.contains("TPT"));
		Assert.assertTrue(body.contains("HO"));
		Assert.assertTrue(body.contains("0:15:40"));
	}
	
	@Test
	public void testVersionsTemplateWithBranchOldFormat()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String, String> testProperties1 = new HashMap<String, String>();
		testProperties1.put("SWIT2-velocity1_Version", "1.1.1");
		testProperties1.put("SWIT1-velocity3_Version", "1.2.3");
		
		Map<String, String> testProperties2 = new HashMap<String, String>();
		testProperties2.put("SWIT-velocity1_Version", "1.2.0");
		testProperties2.put("SWIT-velocity4_Version", "1.2.3");
		
		ElasticsearchTest t1 = new ElasticsearchTest();
		ElasticsearchTest t2 = new ElasticsearchTest();
		t1.setProperties(testProperties1);
		t2.setProperties(testProperties2);
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		tests.add(t1);
		tests.add(t2);
		
		String branchName = "foo";
		String scenarioName = "scenarioName";
		String version = "1.0.00";
		String versionPath = "\\\\FS4\\Projects\\Development\\Internal\\Builds\\15.50\\14_15_50_422\\release";
		String description = "abcdefg";
		
		Map<String,String> scenarioProperties = new HashMap<String,String>();
		scenarioProperties.put("EnbType_Harmony67", "AirHarmony4000, Active cells: 1");
		scenarioProperties.put("EnbType_Unity66", "AirUnity540, Active cells: 1");
		scenarioProperties.put("RadioProfiles", "TDD: Band41, FC2, 20MHz (Harmony67); TDD: Band41, FC2, 20MHz (Unity66)");
		
		String body = templates.populateVersionsAndLinksTemplate(scenarioName, version, branchName, versionPath, description, ExecutionUtils.getAllDevicesVersionsFromTestsProperties(tests), "http://127.0.0.1/reports/exec_9095/index.html", "2017/11/05", "09:08:00 UTC", "5h6m",
				ExecutionUtils.getAllDevicesDetails(scenarioProperties));
	//	Assert.assertTrue(body.contains(branchName));
	//	Assert.assertTrue(body.contains("2017/11/05"));
	//	Assert.assertTrue(body.contains("05/11/2017"));
		Assert.assertTrue(body.contains("\\\\FS4\\Projects\\Development\\Internal\\Builds\\15.50\\14_15_50_422\\release"));
		Assert.assertTrue(body.contains(">14_15_50_422<"));
	//	Assert.assertTrue(body.contains(">14_15_50_422</a></td>"));
	//	Assert.assertTrue(body.contains("Last CL"));
	//	Assert.assertTrue(body.contains(description));
		Assert.assertTrue(body.contains(versionPath));
		Assert.assertTrue(body.contains(version));
	//	Assert.assertTrue(body.contains("velocity1 Build: " + "1.2.0"));
	//	Assert.assertTrue(body.contains("velocity4 Build: " + "1.2.3"));
		Assert.assertTrue(body.contains("http://127.0.0.1/reports/exec_9095/index.html"));
		Assert.assertTrue(body.contains("Harmony67"));
		Assert.assertTrue(body.contains("TDD"));
		Assert.assertTrue(body.contains("41"));
		Assert.assertTrue(body.contains("20MHz"));
		Assert.assertTrue(body.contains("AirHarmony4000"));
	}
	
	@Test
	public void testVersionsTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String, String> testProperties1 = new HashMap<String, String>();
		testProperties1.put("SWIT2-velocity1_Version", "1.1.1");
		testProperties1.put("SWIT1-velocity3_Version", "1.2.3");
		
		Map<String, String> testProperties2 = new HashMap<String, String>();
		testProperties2.put("SWIT-velocity1_Version", "1.2.0");
		testProperties2.put("SWIT-velocity4_Version", "1.2.3");
		
		ElasticsearchTest t1 = new ElasticsearchTest();
		ElasticsearchTest t2 = new ElasticsearchTest();
		t1.setProperties(testProperties1);
		t2.setProperties(testProperties2);
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		tests.add(t1);
		tests.add(t2);
		
		String branchName = "foo";
		String scenarioName = "scenarioName";
		String version = "1.0.00";
		String versionPath = "\\\\FS4\\Projects\\Development\\Internal\\Builds\\15.50\\14_15_50_422\\release";
		String description = "abcdefg";
		
		Map<String,String> scenarioProperties = new HashMap<String,String>();
		scenarioProperties.put("EnbType", "Harmony67,AirHarmony4000,Active cells:1;Unity66,AirUnity540,Active cells:1;");
		scenarioProperties.put("RadioProfiles", "TDD: Band41, FC2, 20MHz (Harmony67); TDD: Band41, FC2, 20MHz (Unity66)");
		
		String body = templates.populateVersionsAndLinksTemplate(scenarioName, version, branchName, versionPath, description, ExecutionUtils.getAllDevicesVersionsFromTestsProperties(tests), "http://127.0.0.1/reports/exec_9095/index.html", "2017/11/05", "09:08:00 UTC", "5h6m",
				ExecutionUtils.getAllDevicesDetails(scenarioProperties));
		Assert.assertTrue(body.contains("\\\\FS4\\Projects\\Development\\Internal\\Builds\\15.50\\14_15_50_422\\release"));
		Assert.assertTrue(body.contains(">14_15_50_422<"));
		Assert.assertTrue(body.contains(versionPath));
		Assert.assertTrue(body.contains(version));
		Assert.assertTrue(body.contains("http://127.0.0.1/reports/exec_9095/index.html"));
		Assert.assertTrue(body.contains("Harmony67"));
		Assert.assertTrue(body.contains("Unity66"));
		Assert.assertTrue(body.contains("TDD"));
		Assert.assertTrue(body.contains("41"));
		Assert.assertTrue(body.contains("20MHz"));
		Assert.assertTrue(body.contains("AirHarmony4000"));
	}

	@Test
	public void testVersionsTemplateWithoutBranch()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String, String> enbs= new HashMap<String, String>();
		enbs.put("foo0", "1.1.3");
		enbs.put("foo1", "1.1.8");
		String scenarioName = "scenarioName";
		String version = "1.0.00";
		String body = templates.populateVersionsAndLinksTemplate(scenarioName, version, null, null, null, enbs, "http://127.0.0.1:9000/reports/exec_9095/index.html", "2/11/17", "09:08:00 UTC", "4h5m", null);
		Assert.assertFalse(body.contains("Branch"));
		Assert.assertTrue(body.contains(version));
	}
	
	@Test
	public void testTableTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String,String> properties = new HashMap<String,String>();
		properties.put("SetupName", "SWIT9");
		properties.put("EnbType_Harmony17", "AirHarmony4400, Active cells: 2");
		properties.put("EnbType_Velocity16", "AirVelocity1000, Active cells: 1");
		Map<String,String> setupConfiguration = ExecutionUtils.getSetupConfiguration(properties);

		String body = templates.populateSetupConfigurationTableTemplate(properties.get("SetupName"), setupConfiguration);
		Assert.assertTrue(body.contains("SWIT9"));
		Assert.assertFalse(body.contains("System defaults profile"));
		Assert.assertFalse(body.contains("RF Conditions"));
	}
	
	@Test
	public void testComparisonTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		final ExecutionStatus currentExecutionStatus = new ExecutionStatus(100, "Sanity", "1.1.1", 15, 0, 1, "0:07:50");
		final ExecutionStatus previousExecutionStatus = new ExecutionStatus(101, "Sanity", "1.1.2", 10, 5, 2, "0:07:50");
		final ComparisonResult comparisonResult = new ComparisonResult();
		final String body = templates.populateComparisonTemplate(currentExecutionStatus, previousExecutionStatus,
				comparisonResult);
		Assert.assertNotNull(body);
		Assert.assertTrue(body.contains("0"));
		Assert.assertTrue(body.contains("1.1.1 vs 1.1.2:"));
	}
	
	@Test
	public void testImageOldFormatTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		ElasticsearchTest test1 = new ElasticsearchTest();
		ElasticsearchTest test2 = new ElasticsearchTest();
		Map<String,String> properties1 = new HashMap<String,String>();
		Map<String,String> properties2 = new HashMap<String,String>();
		properties1.put("Img_123_foo", "image1.png");
		properties2.put("Img_345_goo", "image2.png");
		test1.setProperties(properties1);
		test2.setProperties(properties2);
		test1.setUrl("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439891-1/test.html");
		test2.setUrl("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439892-1/test.html");
		tests.add(test1);
		tests.add(test2);
		
		//String body = templates.populateGraphsTemplate(ExecutionUtils.getAllGraphsLinksOldFormat(tests));
		String body = MailBodyUtils.getGraphs(tests);
		
		Assert.assertTrue(body.contains("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439891-1/image1.png"));
		Assert.assertTrue(body.contains("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439892-1/image2.png"));
	}
	
	@Test
	public void testImageTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		ElasticsearchTest test1 = new ElasticsearchTest();
		ElasticsearchTest test2 = new ElasticsearchTest();
		Map<String,String> properties1 = new HashMap<String,String>();
		Map<String,String> properties2 = new HashMap<String,String>();
		properties1.put("Images", "testName,image1.png;");
		properties2.put("Images", "testName,image1.png;testName,image2.png");
		test1.setProperties(properties1);
		test2.setProperties(properties2);
		test1.setUrl("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439891-1/test.html");
		test2.setUrl("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439892-1/test.html");
		tests.add(test1);
		tests.add(test2);
		
		//String body = templates.populateGraphsTemplate(ExecutionUtils.getAllGraphsLinksOldFormat(tests));
		String body = MailBodyUtils.getGraphs(tests);
		
		Assert.assertTrue(body.contains("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439891-1/image1.png"));
		Assert.assertTrue(body.contains("http://0.0.0.0:9000/reports/exec_362/tests/test_3391471439892-1/image2.png"));
	}
	
	@Test
	public void testGraphsTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String, String> graphsTemp = new HashMap<String, String>();
		graphsTemp.put("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png" , "title1");
		graphsTemp.put("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf2.png", "title2");
		graphsTemp.put("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png", "title3");
		graphsTemp.put("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png", "title4");
		graphsTemp.put("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png", "title5");
		
		String body = templates.populateGraphsTemplate(graphsTemp);
		Assert.assertTrue(body.contains("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png"));
		Assert.assertTrue(body.contains("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf2.png"));
		Assert.assertTrue(body.contains("http://asil-autodev:9000/reports/exec_327/tests/test_1181471345663-1/For%20Shahaf1.png"));
	}
	
	@Test
	public void testTestsTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		int numOfTests = 10;
		// success, warning, failure, error
		for (int i = 0; i < numOfTests; i++) {
			ElasticsearchTest test = new ElasticsearchTest();
			test.setName("test" + i);
			test.setStatus(i % 2 == 0 ? "success" : "failure");
			Map<String, String> props = new LinkedHashMap<String, String>();
			props.put("failureReason", "Something bad happended");
			props.put("foo", "bar");
			props.put("foo", "bar");
			props.put("SWIT1-Velocity"+i+"_Version", Integer.toString(i));
			props.put("SWIT1-Velocity1_Version", "15");
			props.put("CoreFilePath_1234_velocity23a", "http://www.corePath1.com");
			props.put("CoreFilePath_678_velo", "http://www.corePath2.com");
			//props.put("duration", "66741011");
			props.put("foo", "bar");
			test.setProperties(props);
			tests.add(test);
		}
		final String body = templates.populateTestsTemplate(ExecutionUtils.getTestsOrderByEnb(tests));
		Assert.assertTrue(body.contains("Something bad happended"));
		Assert.assertTrue(body.contains("http://www.corePath1.com"));
		Assert.assertTrue(body.contains("velocity23a"));
		Assert.assertTrue(body.contains("http://www.corePath2.com"));
		//Assert.assertTrue(body.contains("18"));
		
		List<ElasticsearchTest> testss = new ArrayList<ElasticsearchTest>();
		ElasticsearchTest test1 = new ElasticsearchTest();
		test1.setName("test1");
		test1.setStatus("success");
		Map<String, String> props1 = new LinkedHashMap<String, String>();
		props1.put("SWIT1-abc_Version", "1.1");
		props1.put("SWIT1-aaa_Version", "6.6");
		test1.setProperties(props1);
		
		ElasticsearchTest test2 = new ElasticsearchTest();
		test2.setName("test2");
		test2.setStatus("success");
		Map<String, String> props2 = new LinkedHashMap<String, String>();
		props2.put("SWIT1-abc_Version", "1.1");
		test2.setProperties(props2);
		
		ElasticsearchTest test3 = new ElasticsearchTest();
		test3.setName("test3");
		test3.setStatus("success");
		Map<String, String> props3 = new LinkedHashMap<String, String>();
		props3.put("SWIT1-def_Version", "3.3");
		test3.setProperties(props3);
		
		testss.add(test1);
		testss.add(test2);
		testss.add(test3);
		
		final String bodyy = templates.populateTestsTemplate(ExecutionUtils.getTestsOrderByEnb(testss));
		Assert.assertTrue(bodyy.contains("aaa"));
		Assert.assertTrue(bodyy.contains("ab"));
		Assert.assertTrue(bodyy.contains("def"));
	}
	
	@Test
	public void testTestsNewFormatTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		ElasticsearchTest test = new ElasticsearchTest();
		test.setName("test" + 1);
		test.setStatus("success");
		
		Map<String, String> props = new LinkedHashMap<String, String>();
		props.put("failureReason", "Something bad happended");
		props.put("CoreFiles", "1,Harmony16,c:\\dasdasdas\\meitala;2,Harmony17,c:\\dasdasdas\\meitalb;");
		props.put("LogCounter", "Density133,<enodeb0:SONERROR>,ERROR,1;Air4G75,prcMngr:PRCMNGR,WARNING,7;Velocity75,Unexpected Reboots,WARNING,3");
		test.setProperties(props);

		List<ElasticsearchTest> lst = new ArrayList<ElasticsearchTest>();
		lst.add(test);
		
		final String body = templates.populateTestsTemplate(ExecutionUtils.getTestsOrderByEnb(lst));
	
		//Assert.assertTrue(body.contains("Unexpected Reboots: 3"));
		//Assert.assertTrue(body.contains("Core Files 1: "));		//<a href='c:\\dasdasdas\\meitala'>Harmony16</a><br>"));
		//Assert.assertTrue(body.contains("Core Files 2: "));		//<a href='c:\\dasdasdas\\meitalb'>Harmony17</a><br>"));
	}
	
	@Test
	public void testErrorsAndWarningsNewFormatTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String,String> senarioProperties = new HashMap<String,String>();
		senarioProperties.put("LogCounter", "Harmony11,prcMngr:PRCMNGR ERROR,ERROR,7;Harmony9,Unexpected Reboots,WARNING,123");
		
		Map<String,String> res1 = new HashMap<String,String>();
		res1 = ExecutionUtils.getAllLineExpressionKeys(senarioProperties.get("LogCounter"));
		Assert.assertTrue(res1.containsKey("EnbLogExpression_ERROR_Harmony11_prcMngr:PRCMNGR ERROR"));
		Assert.assertTrue(res1.containsValue("7"));
		Assert.assertTrue(res1.containsKey("EnbLogExpression_WARNING_Harmony9_Unexpected Reboots"));
		Assert.assertTrue(res1.containsValue("123"));
		

		Set<String> res2 = new TreeSet<String>();
		res2 = ExecutionUtils.getAllEnbsNamesLogExpression(senarioProperties.get("LogCounter"));
		Assert.assertTrue(res2.contains("Harmony11"));
		Assert.assertTrue(res2.contains("Harmony9"));
		
		String body = templates.populateEnbsErrorsAndWarningsTemplate(ExecutionUtils.getLineExpressionMapBySeverity(res1), ExecutionUtils.getLineExpressionMapBySeverity(res1), ExecutionUtils.getAllEnbsNamesLogExpression(senarioProperties.get("LogCounter")));
		
		Assert.assertTrue(body.contains("123"));
		Assert.assertTrue(body.contains("7"));
		Assert.assertTrue(body.contains("Harmony11"));
		Assert.assertTrue(body.contains("Harmony9"));
	}
	
	@Test
	public void testErrorsAndWarningsOldFormatTemplate()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		Map<String,String> properties = new HashMap<String,String>();
		properties.put("EnbLogExpression_ERROR_AirHarmony1000(Harmony11)_prcMngr:PRCMNGR ERROR", "7");
		properties.put("EnbLogExpression_WARNING_AirHarmony1000(Harmony9)_Unexpected Reboots", "123");
		
		
		Map<String,String> res1 = new HashMap<String,String>();
		res1 = ExecutionUtils.getAllLineExpressionKeysOldFormat(properties);
		Assert.assertTrue(res1.containsKey("EnbLogExpression_ERROR_AirHarmony1000(Harmony11)_prcMngr:PRCMNGR ERROR"));
		Assert.assertTrue(res1.containsValue("7"));
		Assert.assertTrue(res1.containsKey("EnbLogExpression_WARNING_AirHarmony1000(Harmony9)_Unexpected Reboots"));
		Assert.assertTrue(res1.containsValue("123"));
		

		Set<String> res2 = new TreeSet<String>();
		res2 = ExecutionUtils.getAllEnbsNamesLogExpressionOldFormat(properties);
		Assert.assertTrue(res2.contains("AirHarmony1000(Harmony11)"));
		Assert.assertTrue(res2.contains("AirHarmony1000(Harmony9)"));
		
		String body = templates.populateEnbsErrorsAndWarningsTemplate(ExecutionUtils.getLineExpressionMapBySeverity(res1), ExecutionUtils.getLineExpressionMapBySeverity(res1), ExecutionUtils.getAllEnbsNamesLogExpressionOldFormat(properties));
		
		Assert.assertTrue(body.contains("123"));
		Assert.assertTrue(body.contains("7"));
		Assert.assertTrue(body.contains("Harmony11"));
		//Assert.assertTrue(body.contains("AirHarmony1000(Harmony11)"));
		Assert.assertTrue(body.contains("Harmony9"));
	}
	
	@Test
	public void testAlarmsAndErorrsTable()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		List<ElasticsearchTest> tests = new ArrayList<ElasticsearchTest>();
		ElasticsearchTest test1 = new ElasticsearchTest();
		test1.setName("test1");
		test1.setStatus("success");
		Map<String, String> props1 = new LinkedHashMap<String, String>();
		props1.put("LogCounter", "Harmony68,enodeb0:DL_SCH,ERROR,64;Harmony68,enodeb0:WR_CUT,ERROR,1;Harmony68,Unexpected Reboots,WARNING,1;Harmony68,enodeb0:ENODEB_SM,ERROR,6;Harmony68,enodeb0:ENODEB_APP,ERROR,5;Harmony68,enodeb0:ENODEB_IPC,ERROR,1;Harmony68,oam:NETWORKING,ERROR,1;Harmony68,enodeb0:XDB,ERROR,3;Harmony68,xsctrl:XSCTRL_X2AP,ERROR,3;Harmony68,xsctrl:XSCTRL_IPC,ERROR,1;Harmony68,prcMngr:PRCMNGR,ERROR,1;Harmony68,enodeb0:APP_TIMERS,ERROR,4;Harmony68,enodeb0:PAL,ERROR,1;");
		props1.put("CoreFiles", null);
		test1.setProperties(props1);
		tests.add(test1);
		
		Map<String,String> senarioProperties = new HashMap<String,String>();
		senarioProperties.put("LogCounter", "Harmony68,enodeb0:DL_SCH,ERROR,117;Harmony68,enodeb0:WR_CUT,ERROR,2;Harmony68,Unexpected Reboots,WARNING,3;Harmony68,enodeb0:ENODEB_SM,ERROR,12;Harmony68,enodeb0:ENODEB_APP,ERROR,12;Harmony68,enodeb0:ENODEB_IPC,ERROR,2;Harmony68,oam:NETWORKING,ERROR,2;Harmony68,oam:SECURITY,ERROR,1;Harmony68,enodeb0:XDB,ERROR,8;Harmony68,enodeb0:RLC,ERROR,1;Harmony68,xsctrl:XSCTRL_X2AP,ERROR,6;Harmony68,xsctrl:XSCTRL_IPC,ERROR,2;Harmony68,prcMngr:PRCMNGR,ERROR,5;Harmony68,enodeb0:APP_TIMERS,ERROR,8;Harmony68,enodeb0:PAL,ERROR,3;");
		
		String body = MailBodyUtils.getErrorsAndWarningsTable(senarioProperties.get("LogCounter"));
		
		Assert.assertTrue(body.contains("Harmony68"));
		Assert.assertTrue(body.contains("enodeb0:DL_SCH"));
		Assert.assertTrue(body.contains("Unexpected Reboots"));
	}
}