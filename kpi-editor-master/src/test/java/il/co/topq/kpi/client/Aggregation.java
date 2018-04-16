package il.co.topq.kpi.client;

import okhttp3.OkHttpClient;

public class Aggregation extends AbstractResource {

	public Aggregation(OkHttpClient client, String baseUrl) {
		super(client, baseUrl);
	}

	public SwFailures swFailures() {
		return new SwFailures(client, baseUrl);
	}

	public Issues issues() {
		return new Issues(client, baseUrl);
	}

}
