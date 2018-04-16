package il.co.topq.kpi.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.topq.kpi.model.ElasticsearchTest;

public class TestTableView extends AbstractTableView<ElasticsearchTest> {

	private enum Header implements TableHeader {

		// @formatter:off
		EXECUTION("Execution"),
		UID("Uid"),
		NAME("Name"),
		DESCRIPTION("Description"),
		STATUS("Status"),
		FAILURE_REASON("Failure Reason"),
		ISSUE_TYPE("Issue Type");
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

	public TestTableView() {
		super(Header.DESCRIPTION);
	}

	@Override
	public AbstractTableView<ElasticsearchTest> populate(List<ElasticsearchTest> tests) {
		for (ElasticsearchTest test : tests) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(Header.EXECUTION.headerName, test.getExecutionId());
			row.put(Header.UID.headerName, test.getUid());
			row.put(Header.NAME.headerName, test.getName());
			row.put(Header.DESCRIPTION.headerName, test.getDescription());
			row.put(Header.STATUS.headerName, test.getStatus());
			row.put(Header.FAILURE_REASON.headerName,
					test.getProperties().get("failureReason") == null ? "" : test.getProperties().get("failureReason"));
			row.put(Header.ISSUE_TYPE.headerName,
					test.getProperties().get("issueType") == null ? "" : test.getProperties().get("issueType"));
			table.addRow(row);

		}
		return this;
	}

}
