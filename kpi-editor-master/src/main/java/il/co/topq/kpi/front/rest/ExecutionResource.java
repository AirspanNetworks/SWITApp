package il.co.topq.kpi.front.rest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.kpi.StopWatch;
import il.co.topq.kpi.model.ElasticDatabase;
import il.co.topq.kpi.model.ElasticsearchTest;
import il.co.topq.kpi.view.AbstractTableView;
import il.co.topq.kpi.view.DataTable;
import il.co.topq.kpi.view.ExecutionTableView;

@RestController
@Path("api/execution")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);
	
	@Autowired
	private ElasticDatabase db;

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public DataTable get() throws IOException {
		log.debug("GET - Get all the executions within the given time frame");
		AbstractTableView<ElasticsearchTest> tableView = new ExecutionTableView();
		StopWatch stopWatch = new StopWatch(log).start("Getting from Elastic all tests within the given time frame");
		List<ElasticsearchTest> tests = db.getTestsByDays(30);
		stopWatch.stopAndLog();
		if (tests.isEmpty()) {
			log.warn("No tests were found in the Elastic");
			return tableView.getTable();
		}

		log.debug("Found " + tests.size() + " tests in the given time frame");
		stopWatch = stopWatch.start("Sorting all tests");
		tests = tests.stream().sorted((test0, test1) -> test1.getExecutionId() - test0.getExecutionId())
				.collect(Collectors.toList());
		stopWatch.stopAndLog();
		
		stopWatch = stopWatch.start("Populating table with tests");
		tableView.populate(tests);
		stopWatch.stopAndLog();
		return tableView.getTable();
	}



}
