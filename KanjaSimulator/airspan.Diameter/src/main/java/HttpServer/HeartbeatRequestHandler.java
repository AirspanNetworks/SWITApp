package HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sun.net.www.protocol.http.HttpURLConnection;

/* Responds to the /test URI. */
public class HeartbeatRequestHandler extends GenericHttpHandler implements HttpHandler {
	private static final Logger log = Logger.getLogger(HeartbeatRequestHandler.class);
	private static final int BUFFER_SIZE = 128;

	public HeartbeatRequestHandler(ArrayList<HttpListener> listeners) {
		super(listeners);
	}

	public void handle(HttpExchange exchange) {
		BufferedReader inFromClient = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		StringBuilder stringBuilder = new StringBuilder();
		char[] charBuffer = new char[BUFFER_SIZE];
		int bytesRead = BUFFER_SIZE;

		log.debug(this); // ALWAYS SAME THREAD!

		while (bytesRead == BUFFER_SIZE) {
			try {
				bytesRead = inFromClient.read(charBuffer);
			} catch (IOException e) {
				log.error("Read Message Request bytes failed due to: "+e);
				e.printStackTrace();
			}
			stringBuilder.append(charBuffer, 0, bytesRead);
		}
		log.debug("HeartbeatRequest Received Message: " + stringBuilder);

		String msisdn = StringUtils.substringBetween(stringBuilder.toString(), "MSISDN=", "&");
		String key = StringUtils.substringBetween(stringBuilder.toString(), "KEY=", "&");

		log.debug("MSISDN: " + msisdn);
		log.debug("KEY: " + key);

		if (msisdn == null || key == null) {
			log.error("HTTP Message should be in this format: MSISDN=xxxx&KEY=xxxx&");
		}

		String bodyResponse = '\n' + "KEY=" + key + "&" + '\n';
		sendHeartbeatMessageResponse(exchange, bodyResponse);
	}

	protected void sendHeartbeatMessageResponse(HttpExchange exchange, String bodyResponse) {
		Headers headers = exchange.getResponseHeaders();
		headers.add("Connetcion", "Close");
		headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headers.add("Content-Length", "100");

		try {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bodyResponse.length());
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(bodyResponse.toString().getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			log.error("Write Message Response failed due to: "+ e);
			e.printStackTrace();
		}
	}
}