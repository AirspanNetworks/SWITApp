package il.co.topq.kpi.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import il.co.topq.elastic.ESClient;
import il.co.topq.elastic.endpoint.Document;
import il.co.topq.kpi.Common;
import il.co.topq.kpi.Configuration;
import il.co.topq.kpi.Configuration.ConfigProps;
import il.co.topq.kpi.StopWatch;

@Component
public class ElasticDatabase {

	private static final Logger log = LoggerFactory.getLogger(ElasticDatabase.class);

	public List<ElasticsearchTest> getTestsByDays(int days) throws JsonProcessingException, IOException {
		List<ElasticsearchTest> tests = new ArrayList<>();
		StopWatch stopWatch = new StopWatch(log).start("Crating instance of ESClient");
		try (ESClient client = new ESClient(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
				Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT))) {
			stopWatch.stopAndLog();
			Map<String, Object> rangeParams = new HashMap<String, Object>();
			rangeParams.put("gte", "now-" + days + "d");
			// @formatter:off
			stopWatch.start("Executing query for all tests withing the given time frame");
			tests = client.index(Common.ELASTIC_INDEX)
					.document(Common.ELASTIC_DOC)
					.search()
					.byRange("executionTimestamp",rangeParams)
					.asClass(ElasticsearchTest.class);
			// @formatter:on
			stopWatch.stopAndLog();
		} catch (Exception e) {
			log.error("Failed to find tests due to " + e.getMessage());
			throw e;
		}
		return tests;

	}
	
	public List<ElasticsearchTest> getTestsByRelease(String release) throws IOException {
		List<ElasticsearchTest> tests = null;
		try (ESClient client = new ESClient(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
				Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT))) {
			tests = client.index(Common.ELASTIC_INDEX).document(Common.ELASTIC_DOC).search()
					.byWildcard("scenarioProperties.targetVersion", release + "*" ).asClass(ElasticsearchTest.class);
		}
		return tests;
	}

	public List<ElasticsearchTest> getTestsByExecution(int executionId) throws IOException {
		List<ElasticsearchTest> tests = null;
		try (ESClient client = new ESClient(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
				Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT))) {
			tests = client.index(Common.ELASTIC_INDEX).document(Common.ELASTIC_DOC).search()
					.byTerm("executionId", executionId + "").asClass(ElasticsearchTest.class);
		}
		return tests;
	}
	
	public void updateTestProperty(String uid, String key,String value) {
		try (ESClient client = new ESClient(Configuration.INSTANCE.readString(ConfigProps.ELASTIC_HOST),
				Configuration.INSTANCE.readInt(ConfigProps.ELASTIC_HTTP_PORT))) {
			final Document document = client.index(Common.ELASTIC_INDEX).document(Common.ELASTIC_DOC);

			List<ElasticsearchTest> tests = document.search().byTerm("uid", uid).asClass(ElasticsearchTest.class);
			if (tests.isEmpty()) {
				log.error("No test with " + uid + " was found. Aborting operation");
				return;
			}

			if (tests.size() > 1) {
				log.error("Unexpected tests number with uid " + uid + ". Found " + tests.size()
						+ " tests. Updating only the first one");
			}
			ElasticsearchTest test = tests.get(0);
			log.debug("Updating test with uid " + uid);
			Map<String, String> properties = test.getProperties();
			if (null == properties) {
				properties = new HashMap<String, String>();
			}
			properties.put(key, value);
			test.setProperties(properties);
			document.update().single(uid, test);

		} catch (IOException e) {
			log.error("Failure due to " + e.getMessage() + " while trying to find or update test with uid " + uid);
		}
	}


}
