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

import Configuration.ConfigurationUtils;
import sun.net.www.protocol.http.HttpURLConnection;

/* Responds to the /test URI. */
public class ModifyBearerRequestHandler extends GenericHttpHandler implements HttpHandler {
	private static final Logger log = Logger.getLogger(ModifyBearerRequestHandler.class);
	private static final int BUFFER_SIZE = 128;
	private static final int DEFAULT_EXPERIMENTAL_RESULT = 5001;
	
	protected ModifyBearerRequestHandler(ArrayList<HttpListener> listeners) {
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
				log.error("failed to read Buffer due to: "+e.getMessage());
				e.printStackTrace();
			}
			stringBuilder.append(charBuffer, 0, bytesRead);
		}
		log.debug("ModifyBearerRequest Received Message: " + stringBuilder);

		String msisdn = StringUtils.substringBetween(stringBuilder.toString(), "MSISDN=", "&");
		String bandwidth = StringUtils.substringBetween(stringBuilder.toString(), "BANDWIDTH=", "&");

		log.debug("MSISDN: " + msisdn);
		log.debug("BANDWIDTH: " + bandwidth);

		if (msisdn == null || bandwidth == null) {
			log.error("HTTP Message should be in this format: MSISDN=xxxx&BANDWIDTH=xxxx&");
		}

		String resultCode = this.handleMBRMessage(msisdn, bandwidth);
		
		int experimentalResult;
		if(ConfigurationUtils.loadHttpFileConfig().getExperimentalResult() != null) {
			experimentalResult = ConfigurationUtils.loadHttpFileConfig().getExperimentalResult();
		}
		else {
			experimentalResult = DEFAULT_EXPERIMENTAL_RESULT;
		}

		String bodyResponse = '\n' + "ResultCode=" + resultCode + "&ExperimentalResult=" +experimentalResult+ "&" + '\n';
		sendModifyBearerMessageResponse(exchange, bodyResponse);
	}

	protected void sendModifyBearerMessageResponse(HttpExchange exchange, String bodyResponse) {
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

	private String handleMBRMessage(String MSISDN, String BANDWIDTH) {
		String ret = "";

		for (HttpListener listener : listeners) {
			if (listener == defaultListener) {
				ret = listener.gotMBRMessage(MSISDN, BANDWIDTH);
				continue;
			}
			listener.gotMBRMessage(MSISDN, BANDWIDTH);
		}
		return ret;
	}
}