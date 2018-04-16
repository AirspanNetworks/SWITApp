package il.co.topq.kpi.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataTable {
	// Holds the headers of the table. The data structure has to be ordered
	// and not to allow duplications.
	private Set<String> headers;
	private List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();

	public DataTable() {

	}

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("headers:").append("\n").append(Arrays.toString(headers.toArray())).append("\n");
		sb.append("data:").append("\n");
		for (Map<String, Object> d : data) {
			for (Map.Entry<String, Object> entry : d.entrySet()) {
				sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
			}
			sb.append("\n");
		}

		return sb.toString();
	}

}
