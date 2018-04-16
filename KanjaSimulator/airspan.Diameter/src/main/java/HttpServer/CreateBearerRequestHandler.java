package HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sun.net.www.protocol.http.HttpURLConnection;

public class CreateBearerRequestHandler extends GenericHttpHandler implements HttpHandler {
	private static final Logger log = Logger.getLogger(CreateBearerRequestHandler.class);
	private static final int BUFFER_SIZE = 128;

	public CreateBearerRequestHandler(ArrayList<HttpListener> listeners) {
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
		log.debug("CreateBearerRequest Received Message: " + stringBuilder);

		String msisdn = StringUtils.substringBetween(stringBuilder.toString(), "MSISDN=", "&");
		String bearer = StringUtils.substringBetween(stringBuilder.toString(), "BEARER=", "&");

		log.debug("MSISDN: " + msisdn);
		log.debug("BEARER: " + bearer);

		if (msisdn == null || bearer == null) {
			log.error("HTTP Message should be in this format: MSISDN=xxxx&BEARER=xx&");
		}

		String resultCode = "";
		if (bearer.toString().equals("D1")) {
			log.debug("D1 Message Received");
			resultCode = this.handleD1Message(msisdn);
		} else if (bearer.toString().equals("D2")) {
			log.debug("D2 Message Received");
			resultCode = this.handleD2Message(msisdn);
		} else {
			log.error("BEARER value should be equals D1 or D2");
		}

		String uniqueID = StringUtils.substringBefore(UUID.randomUUID().toString(), "-");
		String key = uniqueID+msisdn;
		
		String bodyResponse = '\n' + "KEY=" + key + "&ResultCode=" + resultCode + "&" + '\n';
		sendCreateBearerResponse(exchange, bodyResponse);
	}

	private String handleD1Message(String MSISDN) {
		String ret = "";
		for (HttpListener listener : listeners) {
			if (listener == defaultListener) {
				ret = listener.gotD1Message(MSISDN);
				continue;
			}
			listener.gotD1Message(MSISDN);
		}
		return ret;
	}

	private String handleD2Message(String MSISDN) {
		String ret = "";
		for (HttpListener listener : listeners) {
			if (listener == defaultListener) {
				ret = listener.gotD2Message(MSISDN);
				continue;
			}
			listener.gotD1Message(MSISDN);
		}
		return ret;
	}

	protected static void sendCreateBearerResponse(HttpExchange exchange, String bodyResponse) {
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