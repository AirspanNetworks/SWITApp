package il.co.topq.difido.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.swit.ComparisonResult;
import com.swit.TestUtils;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.report.business.elastic.ElasticsearchTest;

public class ExecutionComparisonTests {
	private List<ElasticsearchTest> currentTests;
	private List<ElasticsearchTest> previousTests;

	@Before
	public void setup() {
		currentTests = new ArrayList<ElasticsearchTest>();
		previousTests = new ArrayList<ElasticsearchTest>();
	}

	@Test
	public void testCompareEquals() {
		
		//	Creating tests. Fixture
		ElasticsearchTest test1 = new ElasticsearchTest();
		ElasticsearchTest test2 = new ElasticsearchTest();
		test1.setName("mytest");
		test2.setName("mytest");
		Map<String, String> parameters1 = new HashMap<String, String>();
		Map<String, String> parameters2 = new HashMap<String, String>();
		parameters1.put("foo", "bar");
		parameters2.put("foo", "bar");
		test1.setParameters(parameters1);
		test2.setParameters(parameters2);
		test1.setStatus("failure");
		test2.setStatus("success");
		previousTests.add(test1);
		currentTests.add(test2);

		// Action
		//ComparisonResult(passToFail, passToWarning, failToPass, failToWarning, warningToPass, warningToFail, newTests, deleteTests)
		ComparisonResult result = ComparisonResult.compareExecutions(currentTests, previousTests);
		
		// Assertion
		Assert.assertEquals(1, result.getFailToPass());
	}

	@Test
	public void testComparisonResult()
			throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException {
		//final ExecutionStatus currentExecutionStatus = new ExecutionStatus(100, "Sanity", 15, 0, 1);
		//final ExecutionStatus previousExecutionStatus = new ExecutionStatus(101, "Sanity", 10, 5, 2);
		List<ElasticsearchTest> testsPerv = new ArrayList<ElasticsearchTest>();
		List<ElasticsearchTest> testCurrent = new ArrayList<ElasticsearchTest>();
		final ElasticsearchTest elasticTest0 = new ElasticsearchTest();
		final ElasticsearchTest elasticTest1 = new ElasticsearchTest();
		
		final ElasticsearchTest elasticTest2 = new ElasticsearchTest();
		final ElasticsearchTest elasticTest3 = new ElasticsearchTest();
		
		elasticTest0.setName("test1");
		elasticTest0.setStatus(Status.success.name());
		elasticTest1.setName("test1");
		elasticTest1.setStatus(Status.failure.name());
		

		elasticTest2.setName("test2");
		elasticTest2.setStatus(Status.success.name());
		elasticTest3.setName("test2");
		elasticTest3.setStatus(Status.success.name());
		
		testsPerv.add(elasticTest0);
		testCurrent.add(elasticTest1);

		testsPerv.add(elasticTest2);
		testCurrent.add(elasticTest3);
		
		// Action
		final ComparisonResult comparisonResult  = ComparisonResult.compareExecutions(testCurrent, testsPerv);
		boolean flag = TestUtils.isTestsEquals(elasticTest0, elasticTest1);
		
		// Assertion
		Assert.assertTrue(flag);
		Assert.assertEquals(comparisonResult.getPassToFail(), 1);
	}
}
