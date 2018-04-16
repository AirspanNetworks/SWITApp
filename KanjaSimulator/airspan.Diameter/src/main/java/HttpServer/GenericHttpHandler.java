package HttpServer;

import java.util.ArrayList;

import org.apache.log4j.Logger;

public class GenericHttpHandler {
	protected ArrayList<HttpListener> listeners = new ArrayList<HttpListener>();
	protected HttpListener defaultListener;
	private static final Logger log = Logger.getLogger(GenericHttpHandler.class);

	protected GenericHttpHandler(ArrayList<HttpListener> listeners) {
		this.listeners = listeners;
		defaultListener = this.listeners.get(0);
	}
}