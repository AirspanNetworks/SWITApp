package il.co.topq.kpi;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import il.co.topq.elastic.ESClient;
import il.co.topq.kpi.client.Client;
import il.co.topq.kpi.model.ElasticsearchTest;
import il.co.topq.kpi.utils.ResourceUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest({ "server.port=8080" })
public abstract class AbstractTestCase {

	protected static final String INDEX = "testing";

	protected static final String DOCUMENT = Common.ELASTIC_DOC;

	protected static final String SW_ISSUE = "SW";

	protected static final String SETUP_ISSUE = "Setup";

	protected static final String AUTO_ISSUE = "Auto";

	protected static final SimpleDateFormat ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	@Value("${local.server.port}")
	private int port = 8080;

	// @Value("${local.elastic.port}")
	private int elasticPort = 9200;

	protected ESClient esClient;

	protected Client client;

	protected int executionId = 9000;

	@Before
	public void setup() throws IOException {
		Common.ELASTIC_INDEX = INDEX;
		esClient = new ESClient("localhost", elasticPort);
		client = new Client("http://localhost:" + port);
		recreateIndex();
	}

	private void recreateIndex() throws IOException {
		if (esClient.index(INDEX).isExists()) {
			esClient.index(INDEX).delete();
		}

		try {
			esClient.index(Common.ELASTIC_INDEX).create(ResourceUtils.resourceToString("mapping.json"));

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	protected void addTests(int numOfTests, String type, String branch, String status, String issueType)
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

			esClient.index(INDEX).document(DOCUMENT).add().single(uid + "", test);

		}
	}

	@After
	public void teardown() throws IOException {
		esClient.index(INDEX).delete();
	}

}
