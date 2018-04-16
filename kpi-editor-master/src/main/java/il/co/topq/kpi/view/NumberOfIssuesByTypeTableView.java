package il.co.topq.kpi.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.topq.kpi.model.ElasticsearchTest;

public class NumberOfIssuesByTypeTableView extends AbstractTableView<ElasticsearchTest> {

	public NumberOfIssuesByTypeTableView() {
		super(Header.SETUP_ISSUES);
	}

	public enum Header implements TableHeader {

		// @formatter:off
		SETUP_ISSUES("Setup issues"),
		AUTOMATION_ISSUES("Automation Issues"),
		SW_ISSUES("SW Issues");
		
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
	
	private static long countTestsWithIssueType(List<ElasticsearchTest> tests, String issueType){
		return tests.stream().filter(test -> test.getProperties() != null).filter(test -> issueType.equals(test.getProperties().get("issueType"))).count();
	}

	
	@Override
	public AbstractTableView<ElasticsearchTest> populate(List<ElasticsearchTest> tests) {
		final Map<String, Object> row = new HashMap<String, Object>();
		row.put(Header.SETUP_ISSUES.getHeaderName(), countTestsWithIssueType(tests, "Setup"));
		row.put(Header.AUTOMATION_ISSUES.getHeaderName(), countTestsWithIssueType(tests, "Auto"));
		row.put(Header.SW_ISSUES.getHeaderName(), countTestsWithIssueType(tests, "SW"));
		
		table.addRow(row);
		return this;
	}

}
