package HttpServer;

public interface HttpListener {

	public String gotD1Message(String MSISDN);

	public String gotD2Message(String MSISDN);
	
	public String gotMBRMessage(String MSISDN, String BANDWIDTH);
	
	public String gotStatusRequest();
}