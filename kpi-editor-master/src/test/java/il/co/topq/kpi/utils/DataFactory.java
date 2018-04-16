package il.co.topq.kpi.utils;

import java.io.Closeable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import il.co.topq.elastic.ESClient;
import il.co.topq.kpi.Common;
import il.co.topq.kpi.model.ElasticsearchTest;

/**
 * The purpose of this class is to create data in the real server for manually
 * testing the server. <br>
 * This is not part of the automated tests.
 * 
 * @author itai
 *
 */
public class DataFactory implements Closeable {

	protected static final SimpleDateFormat ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	private static final String INDEX = "report";

	private static final String DOCUMENT = "test";

	private int executionId = 9999;

	private ESClient client;

	public DataFactory() throws IOException {
		client = new ESClient("localhost", 9200);
		createIndexIfNoneExists();
	}

	private void createIndexIfNoneExists() throws IOException {
		try {
			if (client.index(INDEX).isExists()) {
				client.index(INDEX).delete();
			}
			client.index(Common.ELASTIC_INDEX).create(ResourceUtils.resourceToString("mapping.json"));

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void createData() throws IOException {
		 addTests(10, "regression", "12.12.122", "failure", "SW");
		 addTests(20, "regression", "12.12.122", "failure", "Auto");
		 addTests(30, "regression", "12.12.122", "failure", "Setup");
		 addTests(30, "regression", "12.12.122", "success", "");
		 addTests(10, "regression", "13.12.122", "failure", "SW");
		 addTests(20, "regression", "13.12.122", "failure", "Auto");
		 addTests(30, "regression", "13.12.122", "failure", "Setup");
		 addTests(30, "regression", "13.12.122", "success", "");
		 addTests(10, "progression", "12.12.122", "failure", "SW");
		 addTests(20, "progression", "12.12.122", "failure", "Auto");
		 addTests(30, "progression", "12.12.122", "failure", "Setup");
		 addTests(30, "progression", "12.12.122", "success", "");
		 addTests(10, "progression", "13.12.122", "failure", "SW");
		 addTests(20, "progression", "13.12.122", "failure", "Auto");
		 addTests(30, "progression", "13.12.122", "failure", "Setup");
		 addTests(30, "progression", "13.12.122", "success", "");
		 addTests(10, "sanity", "12.12.122", "failure", "SW");
		 addTests(20, "sanity", "12.12.122", "failure", "Auto");
		 addTests(30, "sanity", "12.12.122", "failure", "Setup");
		 addTests(30, "sanity", "12.12.122", "success", "");
		 addTests(10, "sanity", "13.12.122", "failure", "SW");
		 addTests(20, "sanity", "13.12.122", "failure", "Auto");
		 addTests(30, "sanity", "13.12.122", "failure", "Setup");
		 addTests(30, "sanity", "13.12.122", "success", "");

	}

	private void addTests(int numOfTests, String type, String branch, String status, String issueType)
			throws IOException {
		String executionTimeStamp = ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER.format(new Date());
		executionId++;
		for (int i = 0; i < numOfTests; i++) {
			String uid = executionId + "-" + i + "";
			ElasticsearchTest test = new ElasticsearchTest();
			test.setExecutionId(executionId);
			test.setDuration(100);
			test.setExecution("testExecution");
			test.setMachine("ITAI-MACHINE");
			test.setUid(uid);
			test.setName("MyTest");
			test.setDescription("Test all kind of things");
			test.setStatus(status);
			test.setExecutionTimeStamp(executionTimeStamp);

			Map<String, String> scenarioProp = new HashMap<String, String>();
			scenarioProp.put("Type", type);
			scenarioProp.put("Branch", branch);
			test.setScenarioProperties(scenarioProp);

			Map<String, String> testProp = new HashMap<String, String>();
			testProp.put("issueType", issueType);
			test.setProperties(testProp);

			client.index(INDEX).document(DOCUMENT).add().single(uid + "", test);

		}
	}

	@Override
	public void close() throws IOException {
		if (client != null) {
			client.close();
			
		}
	}

	public static void main(String[] args) throws IOException {
		try (DataFactory factory = new DataFactory()){
			factory.createData();
		}
	}


}
