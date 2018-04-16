package il.co.topq.kpi.view;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractTableView<T> {
	
	
	protected final DataTable table;
	
	public AbstractTableView(TableHeader tableHeaders){
		final Set<String> headers = new LinkedHashSet<String>();
		for (TableHeader header : tableHeaders.headers()) {
			headers.add(header.getHeaderName());
		}
		table = new DataTable(headers);
	}
	
	public abstract AbstractTableView<T> populate(List<T> tests);

	public DataTable getTable() {
		return table;
	}
	
	
	
}
