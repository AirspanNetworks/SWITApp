package il.co.topq.kpi.front.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import il.co.topq.elastic.endpoint.Search;
import il.co.topq.elastic.response.query.SearchResponse;
import il.co.topq.elastic.response.query.SearchResponseHandler;
import il.co.topq.kpi.Common;
import il.co.topq.kpi.StopWatch;
import il.co.topq.kpi.model.ElasticDatabase;
import il.co.topq.kpi.model.ElasticsearchTest;
import il.co.topq.kpi.view.ExecutionTableView;

@RestController
@Path("api/release")
public class ReleaseResource {

	private static final Logger log = LoggerFactory.getLogger(ReleaseResource.class);
	
	@Autowired
	private ElasticDatabase db;

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Vector get() throws IOException {
		Vector vec = new Vector();
		//String query = "{\"from\": 0,\"size\": 5000,\"sort\": {\"_score\": {\"order\": \"desc\"}},\"fields\": [\"scenarioProperties.Branch\"]}";
		String query = "{\"size\": 0,\"aggs\" : {\"langs\" : {\"terms\" : { \"field\" : \"scenarioProperties.Branch\",  \"size\" : 0 }}}}";
		ElasticResource er = new ElasticResource();
		String ret = er.post(Common.ELASTIC_INDEX, Common.ELASTIC_DOC, query);
		
		final ObjectMapper objectMapper = new ObjectMapper();
		JsonNode array = objectMapper.readValue(ret, JsonNode.class);
		JsonNode aggregations = array.get("aggregations");
		JsonNode langs = aggregations.get("langs");
		JsonNode buckets = langs.get("buckets");
		buckets.forEach(value -> vec.addValue(value.get("key").toString()));
		//String reportKey = object.get("reportKey").textValue();
		
		
		
		
		return vec;
		
		
		
		
		
		
		
		
		/*log.debug("GET - Get all the Releases within the half year");
		Vector vec = new Vector();
		StopWatch stopWatch = new StopWatch(log).start("Getting from Elastic all tests within the given time frame");
		List<ElasticsearchTest> tests = db.getTestsByDays(30);
		stopWatch.stopAndLog();
		if (tests.isEmpty()) {
			log.warn("No tests were found in the Elastic");
			return vec;
		}

		log.debug("Found " + tests.size() + " tests in the given time frame");
		stopWatch = stopWatch.start("Sorting all tests");
		
		HashSet<String> branchSet = new HashSet<>();
		for(ElasticsearchTest test : tests){
			
			if(test.getScenarioProperties() != null && test.getScenarioProperties().containsKey("Branch"))
				branchSet.add(test.getScenarioProperties().get("Branch"));
			//vec.addValue(test.getScenarioProperties().get("Branch"));	
		}
		
		for(String branch : branchSet)
			vec.addValue(branch);	
		return vec;*/
	}



}
