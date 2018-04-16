
package il.co.topq.kpi.front.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.kpi.model.ElasticDatabase;
import il.co.topq.kpi.model.ElasticsearchTest;
import il.co.topq.kpi.view.AbstractTableView;
import il.co.topq.kpi.view.DataTable;
import il.co.topq.kpi.view.TestTableView;

@RestController
@Path("api/execution/{executionId}/test")
public class TestResource {

	public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final Logger log = LoggerFactory.getLogger(TestResource.class);

	@Autowired
	private ElasticDatabase db;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable get(@PathParam("executionId") int executionId) throws IOException {
		log.debug("GET - Get list of all the tests in the current execution");
		List<ElasticsearchTest> tests = db.getTestsByExecution(executionId);
		final AbstractTableView<ElasticsearchTest> view = new TestTableView();
		if (tests.isEmpty()) {
			log.warn("No tests were found in execution with id " + executionId);
			return view.getTable();
		}
		// @formatter:off
		tests = tests
				.stream()
				.filter(test -> !"success".equals(test.getStatus()))
				.sorted((test0, test1) -> compareUid(test0, test1))
				.collect(Collectors.toList());
		// @formatter:off
		return view.populate(tests).getTable();
	}


	@PUT
	@Path("/{uid}")
	public void put(@PathParam("executionId") int executionId, @PathParam("uid") String uid,
			@QueryParam("issue") String issue) {
		log.debug("PUT - Changing test with execution id " + executionId + " and uid " + uid + " to issue " + issue);
		db.updateTestProperty(uid, "issueType", issue);
		log.debug("Finished updating issue type " + issue + " to test with uid " + uid);
	}
	
	private static int compareUid(ElasticsearchTest test0, ElasticsearchTest test1) {
		int test0Id = Integer.parseInt(test0.getUid().substring(test0.getUid().indexOf("-")));
		int test1Id = Integer.parseInt(test1.getUid().substring(test1.getUid().indexOf("-")));
		return test1Id - test0Id;
	}


}
