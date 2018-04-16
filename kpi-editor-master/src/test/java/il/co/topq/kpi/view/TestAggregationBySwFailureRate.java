package il.co.topq.kpi.view;

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;

import org.junit.Test;

import il.co.topq.kpi.AbstractTestCase;
import il.co.topq.kpi.view.DataTable;
import il.co.topq.kpi.view.SwFailureRatesAggsTableView;

public class TestAggregationBySwFailureRate extends AbstractTestCase {

	@Test
	public void testRatesPerTypes() throws Exception {
		String branch = "12.13.121";

		addTests(10, "sanity", branch, "falure", SW_ISSUE);
		addTests(90, "sanity", branch, "success", "");
		
		addTests(20, "regression", branch, "falure", SW_ISSUE);
		addTests(80, "regression", branch, "success", "");
		
		addTests(30, "progression", branch, "falure", SW_ISSUE);
		addTests(70, "progression", branch, "success", "");

		Thread.sleep(1000);

		DataTable table = client.aggregation().swFailures().get("sanity", 1);
		assertEquals("0.1%", extractSwFailureRate(table, branch));

		table = client.aggregation().swFailures().get("regression", 1);
		assertEquals("0.2%", extractSwFailureRate(table, branch));

		table = client.aggregation().swFailures().get("progression", 1);
		assertEquals("0.3%", extractSwFailureRate(table, branch));

	}

	@Test
	public void testRatesPerDifferentIssues() throws Exception {
		String type = "sanity";
		String branch = "12.13.121";

		addTests(10, type, branch, "falure", SW_ISSUE);
		addTests(10, type, branch, "failure", SETUP_ISSUE);
		addTests(10, type, branch, "failure", AUTO_ISSUE);
		addTests(10, type, branch, "success", "");
		addTests(10, type, branch, "warning", "");

		Thread.sleep(1000);

		final DataTable table = client.aggregation().swFailures().get("sanity", 1);
		assertEquals("0.2%", extractSwFailureRate(table, branch));
	}

	@Test
	public void testRatesPerBranches() throws Exception {
		String type = "sanity";
		String branch0 = "12.13.121";
		String branch1 = "13.12.121";
		String branch2 = "14.11.121";

		addTests(1, type, branch0, "falure", SW_ISSUE);
		addTests(9, type, branch0, "success", "");

		addTests(2, type, branch1, "falure", SW_ISSUE);
		addTests(8, type, branch1, "success", "");

		addTests(3, type, branch2, "falure", SW_ISSUE);
		addTests(7, type, branch2, "success", "");

		System.out.println("Going to sleep for one second");
		Thread.sleep(1000);

		final DataTable table = client.aggregation().swFailures().get("sanity", 1);
		assertEquals("0.1%", extractSwFailureRate(table, branch0));
		assertEquals("0.2%", extractSwFailureRate(table, branch1));
		assertEquals("0.3%", extractSwFailureRate(table, branch2));

	}

	private static String extractSwFailureRate(DataTable table, String branch) {
		return table.getData().stream().filter(m -> m.get("Branch").equals(branch)).collect(Collectors.toList()).get(0)
				.get(SwFailureRatesAggsTableView.Header.SW_FAILURE_RATE.getHeaderName()).toString();
	}

}
