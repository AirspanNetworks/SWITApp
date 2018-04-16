package il.co.topq.kpi.client;

import okhttp3.OkHttpClient;

public class Client {

	private final OkHttpClient client;

	private final String baseUrl;

	public Client(String URL) {
		client = new OkHttpClient();
		this.baseUrl = URL + "/api/";
	}

	public Aggregation aggregation() {
		return new Aggregation(client, baseUrl);
	}

}
