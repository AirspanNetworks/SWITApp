package il.co.topq.kpi.front.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Vector {
	final private List<Object> data = new ArrayList<Object>();

	public Vector(Collection<Object> data) {
		data.addAll(data);
	}
	
	public Vector() {

	}
	
	public void addAll(Collection<Object> data) {
		data.addAll(data);
	}

	public void addValue(Object val) {
		data.add(val);
	}

	public List<Object> getData() {
		return data;
	}

}
