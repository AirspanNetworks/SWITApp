package il.co.topq.kpi.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.topq.kpi.model.ElasticsearchTest;

public class SwFailureRatesAggsTableView extends AbstractTableView<ElasticsearchTest> {

	public enum Header implements TableHeader {

		// @formatter:off
		BRANCH("Branch"),
		SW_FAILURE_RATE("SW Failure Rate");
		// @formatter:on

		public final String headerName;

		private Header(String headerName) {
			this.headerName = headerName;
		}

		@Override
		public String getHeaderName() {
			return headerName;
		}

		@Override
		public TableHeader[] headers() {
			return values();
		}

	}

	public SwFailureRatesAggsTableView() {
		super(Header.BRANCH);
	}

	@Override
	public AbstractTableView<ElasticsearchTest> populate(List<ElasticsearchTest> tests) {
		final Map<String, List<ElasticsearchTest>> testsByBranch = new HashMap<>();

		// Sorting tests by branch
		for (ElasticsearchTest test : tests) {
			String build = test.getScenarioProperties().get("Branch");
			if (!testsByBranch.containsKey(build)) {
				testsByBranch.put(build, new ArrayList<ElasticsearchTest>());
			}
			testsByBranch.get(build).add(test);
		}
		int totalTests = 0;
		int totalSwFailures = 0;
		for (String branch : testsByBranch.keySet()) {
			List<ElasticsearchTest> brunchTests = testsByBranch.get(branch);
			int testsPerBranch = brunchTests.size();
			totalTests += testsPerBranch;
			int SwFailuresPerBranch = (int) brunchTests.stream().filter(test -> test.getProperties() != null)
					.filter(test -> "SW".equals(test.getProperties().get("issueType"))).count();
			totalSwFailures += SwFailuresPerBranch;
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(Header.BRANCH.getHeaderName(), branch);
			row.put(Header.SW_FAILURE_RATE.getHeaderName(),
					((float) SwFailuresPerBranch / (float) testsPerBranch) + "%");
			table.addRow(row);
		}
		Map<String, Object> row = new HashMap<String, Object>();
		row.put(Header.BRANCH.getHeaderName(), "Total");
		row.put(Header.SW_FAILURE_RATE.getHeaderName(), ((float) totalSwFailures / (float) totalTests) + "%");
		table.addRow(row);
		return this;
	}

}
