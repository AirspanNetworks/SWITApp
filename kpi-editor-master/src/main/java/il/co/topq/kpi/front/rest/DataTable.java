package il.co.topq.kpi.front.rest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTable {
	// Holds the headers of the table. The data structure has to be ordered
	// and not to allow duplications.
	final private Set<String> headers;
	final private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	public DataTable(Set<String> headers) {
		this.headers = new LinkedHashSet<String>();
		this.headers.addAll(headers);
	}

	public void addRow(Map<String, Object> row) {
		data.add(row);
	}

	public List<Map<String, Object>> getData() {
		return data;
	}

	public Set<String> getHeaders() {
		return headers;
	}

}
