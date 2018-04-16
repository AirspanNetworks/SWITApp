package HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import com.sun.net.httpserver.HttpServer;

import Configuration.ConfigurationUtils;
import Configuration.HttpConfig;

public class HTTP implements Runnable {
	private static final Logger log = Logger.getLogger(HTTP.class);
	private ArrayList<HttpListener> listeners = new ArrayList<HttpListener>();
	private int port;
	private static HTTP serverInstance;
	private HttpServer httpServer;
	private HttpConfig httpConfig;
	private ExecutorService executor;

	public HTTP() {
		this.httpConfig = ConfigurationUtils.loadHttpFileConfig();
		this.port = Integer.getInteger("httpServer.port", httpConfig.getPort());
	}

	@Override
	public void run() {
		try {
			executor = Executors.newFixedThreadPool(1);
			httpServer = HttpServer.create(new InetSocketAddress(this.port), 0);
			httpServer.createContext("/CreateBearerRequest", new CreateBearerRequestHandler(listeners));
			httpServer.createContext("/HeartbeatRequest", new HeartbeatRequestHandler(listeners));
			httpServer.createContext("/ModifyBearerRequest", new ModifyBearerRequestHandler(listeners));
			httpServer.createContext("/Status", new StatusHandler(listeners));
			httpServer.setExecutor(executor);
			httpServer.start();
			log.debug("Started HttpServer at port " + this.port);

			// Wait here until notified of shutdown.
			synchronized (this) {
				try {
					this.wait();
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e.getMessage());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error(t.getMessage());
		}
	}

	static void shutdown() {
		try {
			log.debug("Shutting down TestServer.");
			serverInstance.httpServer.stop(0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (serverInstance) {
			serverInstance.notifyAll();
		}
	}

	public void addListener(HttpListener listener) {
		this.listeners.add(listener);
	}

	public String getStatus() {
		String ret = "";
		ret += "HTTP Server: " + this.httpServer.getAddress().toString() + "\n";
		return ret;
	}
}

/* Responds to a JVM shutdown by stopping the server. */
class OnShutdown extends Thread {
	public void run() {
		HTTP.shutdown();
	}
}