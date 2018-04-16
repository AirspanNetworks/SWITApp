package Controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import Diameter.DiameterClient;
import Diameter.DiameterListener;
import HttpServer.HTTP;
import HttpServer.HttpListener;

public class Controller implements DiameterListener, HttpListener {
	private static final Logger log = Logger.getLogger(Controller.class);
	private HTTP httpServer;
	private DiameterClient diamClient;
	private static Controller instance;
	Map<String,String> aarData=new HashMap<String,String>();

	private Controller() {
		this.httpServer = new HTTP();
		this.diamClient = new DiameterClient();

		httpServer.addListener(this);
		diamClient.addListener(this);
	}

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();

		return instance;
	}

	public void init() {
		this.diamClient.init();

		Thread serverThread = new Thread(httpServer);
		serverThread.start();
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		aarData.put("MSISDN", "");

		this.diamClient.sendAarMessage(MessageType.CBR1, aarData);
	try {
		Thread.sleep(5*1000); 
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	this.diamClient.sendAarMessage(MessageType.CBR2, aarData);

		// Runtime.getRuntime().addShutdownHook(new OnShutdown());

		try {
			serverThread.join();
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public String gotD1Message(String MSISDN) {
		log.debug("got D1 Message");
		aarData.put("MSISDN", MSISDN);
		
		return this.diamClient.sendAarMessage(MessageType.CBR1, aarData);
	}

	@Override
	public String gotD2Message(String MSISDN) {
		log.debug("got D2 Message");
		aarData.put("MSISDN", MSISDN);
		
		return this.diamClient.sendAarMessage(MessageType.CBR2, aarData);
	}

	@Override
	public String gotMBRMessage(String MSISDN, String BANDWIDTH) {
		log.debug("got MBR Message");
		aarData.put("MSISDN", MSISDN);
		aarData.put("BANDWIDTH", BANDWIDTH);
		
		return this.diamClient.sendAarMessage(MessageType.MBR, aarData);
	}

	@Override
	public String gotStatusRequest() {
		String ret = "";

		ret += "Diameter Status\n";
		ret += this.diamClient.getStatus() + "\n";
		ret += "HTTP Status\n";
		ret += this.httpServer.getStatus() + "\n";
		ret += "Java Status\n";
		ret += this.getEnvStatus() + "\n";

		return ret;
	}

	public String getEnvStatus() {
		String ret = "";

		ret += "Available processors (cores): " + Runtime.getRuntime().availableProcessors() + "\n";
		ret += "Free memory : " + humanReadableByteCount(Runtime.getRuntime().freeMemory()) + "\n";
		long maxMemory = Runtime.getRuntime().maxMemory();
		ret += "Maximum memory : " + (maxMemory == Long.MAX_VALUE ? "no limit" : humanReadableByteCount(maxMemory))
				+ "\n";
		ret += "Total memory : " + humanReadableByteCount(Runtime.getRuntime().totalMemory()) + "\n";

		return ret;
	}

	public static String humanReadableByteCount(long bytes) {
		boolean si = false;
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}