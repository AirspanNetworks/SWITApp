package il.co.topq.kpi.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.topq.kpi.model.ElasticsearchTest;

public class ExecutionTableView extends AbstractTableView<ElasticsearchTest> {

	public ExecutionTableView() {
		super(Header.AUTOMATION_ISSUES);
	}

	private enum Header implements TableHeader {

		// @formatter:off
		EXECUTION("Execution"),
		DATE("Date"),
		TYPE("Type"),
		BRANCH("Branch"),
		BUILD("Build"), 
		SETUP("Setup"),
		TOTAL_QUANTITY("Total Quantity"),
		TOTAL_PASS("Total Pass"),
		TOTAL_FAILURES("Total Failures"),
		SETUP_ISSUES("Setup Issues"),
		AUTOMATION_ISSUES("Automation Issues"),
		SW_ISSUES("SW Issues"),
		UNSPECIFIED_ISSUES("Unspecifed Issues");
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
			return Header.values();
		}

	}

	private class ExecutionMetadata {

		final int id;
		final String date;
		final String branch;
		final String setup;
		final String type;
		int totalQuantity = 0;
		int totalPass = 0;
		int totalFaliures = 0;
		int setupIssues = 0;
		int autoIssues = 0;
		int swIssues = 0;
		int unspecIssues = 0;

		private ExecutionMetadata(ElasticsearchTest test) {
			super();
			this.id = test.getExecutionId();
			this.date = test.getExecutionTimeStamp();
			if (test.getScenarioProperties() != null && !test.getScenarioProperties().isEmpty()) {
				branch = test.getScenarioProperties().get("Branch");
				setup = test.getScenarioProperties().get("SetupName");
				type = test.getScenarioProperties().get("Type");
			} else {
				branch = "";
				setup = "";
				type = "";

			}

		}

	}

	public AbstractTableView<ElasticsearchTest> populate(final List<ElasticsearchTest> tests) {
		ExecutionMetadata eData = new ExecutionMetadata(tests.get(0));

		for (ElasticsearchTest test : tests) {
			if (eData.id != test.getExecutionId()) {
				createTableRow(table, eData);
				eData = new ExecutionMetadata(test);
			}
			eData.totalQuantity++;
			if ("success".equals(test.getStatus())) {
				eData.totalPass++;
				continue;
			}
			eData.totalFaliures++;
			if (test.getProperties() == null || test.getProperties().get("issueType") == null) {
				eData.unspecIssues++;
				continue;
			}
			switch (test.getProperties().get("issueType")) {
			case "SW":
				eData.swIssues++;
				break;
			case "Auto":
				eData.autoIssues++;
				break;
			case "Setup":
				eData.setupIssues++;
				break;
			default:
				eData.unspecIssues++;
				break;
			}
		}
		createTableRow(table, eData);
		return this;
	}

	private void createTableRow(final DataTable table, ExecutionMetadata eData) {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put(Header.EXECUTION.headerName, eData.id);
		row.put(Header.DATE.headerName, eData.date);
		row.put(Header.TYPE.headerName, eData.type);
		row.put(Header.BRANCH.headerName, eData.branch);
		row.put(Header.BUILD.headerName, "");
		row.put(Header.SETUP.headerName, eData.setup);
		row.put(Header.TOTAL_QUANTITY.headerName, eData.totalQuantity);
		row.put(Header.TOTAL_PASS.headerName, eData.totalPass);
		row.put(Header.TOTAL_FAILURES.headerName, eData.totalFaliures);
		row.put(Header.SETUP_ISSUES.headerName, eData.setupIssues);
		row.put(Header.AUTOMATION_ISSUES.headerName, eData.autoIssues);
		row.put(Header.SW_ISSUES.headerName, eData.swIssues);
		row.put(Header.UNSPECIFIED_ISSUES.headerName, eData.unspecIssues);
		table.addRow(row);
	}
	
	

}
