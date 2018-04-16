package il.co.topq.kpi.view;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import il.co.topq.kpi.AbstractTestCase;
import il.co.topq.kpi.view.DataTable;
import il.co.topq.kpi.view.NumberOfIssuesByTypeTableView;

public class TestAggregationByIssuesCount extends AbstractTestCase {

	@Test
	public void testIssuesCountPerTestTypeInSingleBranch() throws Exception {
		String branch0 = "12.13.121";
		String testType = "sanity";

		addTests(10, testType, branch0, "falure", SW_ISSUE);
		addTests(20, testType, branch0, "falure", SETUP_ISSUE);
		addTests(30, testType, branch0, "falure", AUTO_ISSUE);
		addTests(90, testType, branch0, "success", "");

		Thread.sleep(1000);

		DataTable table = client.aggregation().issues().get(testType, 1);
		assertEquals(10, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SW_ISSUES.getHeaderName()));
		assertEquals(20, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SETUP_ISSUES.getHeaderName()));
		assertEquals(30,
				table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.AUTOMATION_ISSUES.getHeaderName()));
	}

	@Test
	public void testIssuesCountPerTestTypeInMultipleBranches() throws Exception {
		String branch0 = "12.13.100";
		String branch1 = "13.12.200";
		String branch2 = "14.10.220";
		String testType = "sanity";

		addTests(10, testType, branch0, "falure", SW_ISSUE);
		addTests(20, testType, branch0, "falure", SETUP_ISSUE);
		addTests(30, testType, branch0, "falure", AUTO_ISSUE);
		addTests(90, testType, branch0, "success", "");

		addTests(10, testType, branch1, "falure", SW_ISSUE);
		addTests(20, testType, branch1, "falure", SETUP_ISSUE);
		addTests(30, testType, branch1, "falure", AUTO_ISSUE);
		addTests(90, testType, branch1, "success", "");

		addTests(10, testType, branch2, "falure", SW_ISSUE);
		addTests(20, testType, branch2, "falure", SETUP_ISSUE);
		addTests(30, testType, branch2, "falure", AUTO_ISSUE);
		addTests(90, testType, branch2, "success", "");

		Thread.sleep(1000);

		DataTable table = client.aggregation().issues().get(testType, 1);
		assertEquals(30, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SW_ISSUES.getHeaderName()));
		assertEquals(60, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SETUP_ISSUES.getHeaderName()));
		assertEquals(90,
				table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.AUTOMATION_ISSUES.getHeaderName()));
	}

	@Test
	public void testIssuesCountPerTestTypeInMultipleTestTypes() throws Exception {
		String branch = "12.13.100";
		String testType0 = "sanity";
		String testType1 = "regression";
		String testType2 = "progression";

		addTests(10, testType0, branch, "falure", SW_ISSUE);
		addTests(20, testType0, branch, "falure", SETUP_ISSUE);
		addTests(30, testType0, branch, "falure", AUTO_ISSUE);
		addTests(90, testType0, branch, "success", "");

		addTests(10, testType1, branch, "falure", SW_ISSUE);
		addTests(20, testType1, branch, "falure", SETUP_ISSUE);
		addTests(30, testType1, branch, "falure", AUTO_ISSUE);
		addTests(90, testType1, branch, "success", "");

		addTests(10, testType2, branch, "falure", SW_ISSUE);
		addTests(20, testType2, branch, "falure", SETUP_ISSUE);
		addTests(30, testType2, branch, "falure", AUTO_ISSUE);
		addTests(90, testType2, branch, "success", "");

		Thread.sleep(1000);

		DataTable table = client.aggregation().issues().get(testType0, 1);
		assertEquals(10, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SW_ISSUES.getHeaderName()));
		assertEquals(20, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SETUP_ISSUES.getHeaderName()));
		assertEquals(30,
				table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.AUTOMATION_ISSUES.getHeaderName()));

		table = client.aggregation().issues().get(testType1, 1);
		assertEquals(10, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SW_ISSUES.getHeaderName()));
		assertEquals(20, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SETUP_ISSUES.getHeaderName()));
		assertEquals(30,
				table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.AUTOMATION_ISSUES.getHeaderName()));

		table = client.aggregation().issues().get(testType2, 1);
		assertEquals(10, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SW_ISSUES.getHeaderName()));
		assertEquals(20, table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.SETUP_ISSUES.getHeaderName()));
		assertEquals(30,
				table.getData().get(0).get(NumberOfIssuesByTypeTableView.Header.AUTOMATION_ISSUES.getHeaderName()));

	}

}
