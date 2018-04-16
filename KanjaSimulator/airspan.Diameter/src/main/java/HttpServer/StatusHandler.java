package HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sun.net.www.protocol.http.HttpURLConnection;

/* Responds to the /test URI. */
public class StatusHandler extends GenericHttpHandler implements HttpHandler {
	private static final Logger log = Logger.getLogger(StatusHandler.class);

	protected StatusHandler(ArrayList<HttpListener> listeners) {
		super(listeners);
	}

	public void handle(HttpExchange exchange) {
		log.debug(this); // ALWAYS SAME THREAD!

		String bodyResponse = getStatus();
		sendStatusResponse(exchange, bodyResponse);
	}

	protected void sendStatusResponse(HttpExchange exchange, String bodyResponse) {
		Headers headers = exchange.getResponseHeaders();
		headers.add("Connetcion", "Close");
		headers.add("Content-Length", "100");

		try {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, bodyResponse.length());
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(bodyResponse.toString().getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			log.error("send response failed due to: "+ e);
			e.printStackTrace();
		}
	}

	private String getStatus() {
		String ret = "";
		for (HttpListener lis : listeners) {
			ret += lis.gotStatusRequest();
		}
		return ret;
	}
}