package Diameter;

import static org.jdiameter.api.Avp.MAX_REQUESTED_BANDWIDTH_DL;
import static org.jdiameter.api.Avp.MAX_REQUESTED_BANDWIDTH_UL;
import static org.jdiameter.api.Avp.SUBSCRIPTION_ID;
import static org.jdiameter.api.Avp.SUBSCRIPTION_ID_DATA;
import static org.jdiameter.api.Avp.SUBSCRIPTION_ID_TYPE;
import static org.jdiameter.api.Avp.RESULT_CODE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Configuration;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Message;
import org.jdiameter.api.MetaData;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.Session;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.Stack;
import org.jdiameter.api.StackType;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.dictionary.AvpRepresentation;

import Configuration.ConfigurationUtils;
import Configuration.DiamConfig;
import Configuration.MediaComponentDescription;
import Controller.MessageType;

public class DiameterClient implements EventListener<Request, Answer> {

	private ArrayList<DiameterListener> listeners = new ArrayList<DiameterListener>();
	private static final Logger log = LogManager.getLogger(DiameterClient.class);
	static {
		configLog4j();
	}

	private static void configLog4j() {
		InputStream inStreamLog4j = DiameterClient.class.getClassLoader().getResourceAsStream("log4j.properties");
		Properties propertiesLog4j = new Properties();
		try {
			propertiesLog4j.load(inStreamLog4j);
			PropertyConfigurator.configure(propertiesLog4j);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inStreamLog4j != null) {
				try {
					inStreamLog4j.close();
				} catch (IOException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		log.debug("log4j configured");
	}

	// configuration files
	private static final String configFile = "client-jdiameter-config.xml";
	private static final String dictionaryFile = "dictionary.xml";
	// definition of codes, IDs
	private ApplicationId App3gppRX = ApplicationId.createByAuthAppId(16777236);
	// Dictionary, for informational purposes.
	private AvpDictionary dictionary = AvpDictionary.INSTANCE;
	// stack and session factory
	private Stack stack;
	private SessionFactory factory;
	private Object AAA_WAIT = new Object();
	private String AAAResultCode = "";
	private boolean AAA_Gotresult = false;

	// ////////////////////////////////////////
	// Objects which will be used in action //
	// ////////////////////////////////////////
	private Session session; // session used as handle for communication

	private static final int AA_COMMAND = 265;
	private static final int MEDIA_COMPONENT_DESCRIPTION = 517;
	private static final int MEDIA_SUB_COMPONENT = 519;
	private static final int MEDIA_COMPONENT_NUMBER = 518;
	private static final int MEDIA_TYPE = 520;
	private static final int FLOW_NUMBER = 509;
	private static final int FLOW_DESCRIPTION = 507;

	private static final int FLOW_STATUS = 511;
	private static final int FLOW_USAGE = 512;
	private static final int CODEC_DATA = 524;
	private DiamConfig config;

	public DiameterClient() {
	}

	public void init() {
		this.initStack();
		this.start();
	}

	private void initStack() {
		if (log.isInfoEnabled()) {
			log.info("Initializing Stack...");
		}
		InputStream is = null;
		try {
			// Parse dictionary, it is used for user friendly info.
			dictionary.parseDictionary(this.getClass().getClassLoader().getResourceAsStream(dictionaryFile));
			log.info("AVP Dictionary successfully parsed.");

			this.stack = new StackImpl();
			// Parse stack configuration
			is = new FileInputStream(configFile);
			Configuration config = new XMLConfiguration(is);
			factory = stack.init(config);
			
			if (log.isInfoEnabled()) {
				log.info("Stack Configuration successfully loaded.");
			}
			// Print info about applicatio
			Set<org.jdiameter.api.ApplicationId> appIds = stack.getMetaData().getLocalPeer().getCommonApplications();

			log.info("Diameter Stack  :: Supporting " + appIds.size() + " applications.");
			for (org.jdiameter.api.ApplicationId x : appIds) {
				log.info("Diameter Stack  :: Common :: " + x);
			}
			is.close();
			// Register network req listener, even though we wont receive
			// requests
			// this has to be done to inform stack that we support application
		/*	Network network = stack.unwrap(Network.class);
			network.addNetworkReqListener(new NetworkReqListener() {

				@Override
				public Answer processRequest(Request request) {
					// this wontbe called.
					return null;
				}
			}, new ApplicationId[] { this.App3gppRX });*/

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			if (this.stack != null) {
				this.stack.destroy();
			}

			if (is != null) {
				try {
					is.close();
				} catch (IOException e1) {
					log.error(e1.getMessage());
					e1.printStackTrace();
				}
			}
			return;
		}

		MetaData metaData = stack.getMetaData();
		// ignore for now.
		if (metaData.getStackType() != StackType.TYPE_SERVER || metaData.getMinorVersion() <= 0) {
			stack.destroy();
			if (log.isEnabledFor(org.apache.log4j.Level.ERROR)) {
				log.error("Incorrect driver");
			}
			return;
		}

		try {
			if (log.isInfoEnabled()) {
				log.info("Starting stack");
			}
			stack.start();
			if (log.isInfoEnabled()) {
				log.info("Stack is running.");
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
			stack.destroy();
			return;
		}
		if (log.isInfoEnabled()) {
			log.info("Stack initialization successfully completed.");
		}
	}

	private void start() {
		try {
			// wait for connection to peer
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
			this.session = this.factory.getNewSession();

		} catch (InternalException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.jdiameter.api.EventListener#receivedSuccessMessage(org.jdiameter
	 * .api.Message, org.jdiameter.api.Message)
	 */
	@Override
	public void receivedSuccessMessage(Request request, Answer answer) {
		dumpMessage(answer, false);
		log.debug("Received Success Message");

		if (answer.getCommandCode() == AA_COMMAND) {
			AvpSet answerAvpSet = answer.getAvps();
			try {
				AAAResultCode = answerAvpSet.getAvp(RESULT_CODE, 0).getInteger32() + "";
			} catch (AvpDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error(e.getMessage());
			}
			synchronized (AAA_WAIT) {
				AAA_Gotresult = true;
				AAA_WAIT.notifyAll();
			}
			return;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.jdiameter.api.EventListener#timeoutExpired(org.jdiameter.api.
	 * Message)
	 */
	@Override
	public void timeoutExpired(Request request) {

	}

	private void dumpMessage(Message message, boolean sending) {
		if (log.isInfoEnabled()) {
			log.info((sending ? "Sending " : "Received ") + (message.isRequest() ? "Request: " : "Answer: ")
					+ message.getCommandCode() + "\nE2E:" + message.getEndToEndIdentifier() + "\nHBH:"
					+ message.getHopByHopIdentifier() + "\nAppID:" + message.getApplicationId());
			log.info("AVPS[" + message.getAvps().size() + "]: \n");
			try {
				printAvps(message.getAvps());
			} catch (AvpDataException e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void printAvps(AvpSet avpSet) throws AvpDataException {
		printAvpsAux(avpSet, 0);
	}

	/**
	 * Prints the AVPs present in an AvpSet with a specified 'tab' level
	 *
	 * @param avpSet
	 *            the AvpSet containing the AVPs to be printed
	 * @param level
	 *            an int representing the number of 'tabs' to make a pretty
	 *            print
	 * @throws AvpDataException
	 */
	private void printAvpsAux(AvpSet avpSet, int level) throws AvpDataException {
		String prefix = "                      ".substring(0, level * 2);

		for (Avp avp : avpSet) {
			AvpRepresentation avpRep = AvpDictionary.INSTANCE.getAvp(avp.getCode(), avp.getVendorId());

			if (avpRep != null && avpRep.getType().equals("Grouped")) {
				log.info(prefix + "<avp name=\"" + avpRep.getName() + "\" code=\"" + avp.getCode() + "\" vendor=\""
						+ avp.getVendorId() + "\">");
				printAvpsAux(avp.getGrouped(), level + 1);
				log.info(prefix + "</avp>");
			} else if (avpRep != null) {
				String value = "";

				if (avpRep.getType().equals("Integer32"))
					value = String.valueOf(avp.getInteger32());
				else if (avpRep.getType().equals("Integer64") || avpRep.getType().equals("Unsigned64"))
					value = String.valueOf(avp.getInteger64());
				else if (avpRep.getType().equals("Unsigned32"))
					value = String.valueOf(avp.getUnsigned32());
				else if (avpRep.getType().equals("Float32"))
					value = String.valueOf(avp.getFloat32());
				else
					value = new String(avp.getOctetString(), StandardCharsets.UTF_8);

				log.info(prefix + "<avp name=\"" + avpRep.getName() + "\" code=\"" + avp.getCode() + "\" vendor=\""
						+ avp.getVendorId() + "\" value=\"" + value + "\" />");
			}
		}
	}
	private String sendD1AarMessage(Map<String, String> data,String port) {
		MediaComponentDescription mediaDes = new MediaComponentDescription();
		int subscriptionIdType = 0;
		this.config = ConfigurationUtils.loadDiamConfigFile();
		mediaDes = this.config.getCBR1().getMediaComponentDescription();
		subscriptionIdType = this.config.getCBR1().getSubscriptionIdType();
	
		String subscriptionId = data.get("MSISDN");
		
		String serverHost = stack.getMetaData().getConfiguration().getChildren(37)[0].getChildren(23)[0].getStringValue(79,"").split(":")[0];
		String realmName = this.stack.getMetaData().getLocalPeer().getRealmName();
		Request request = session.createRequest(AA_COMMAND, this.App3gppRX, realmName,serverHost +":" + port);
		request.setRequest(true);
		request.setProxiable(true); 
		AvpSet requestAvps = request.getAvps();
		
		requestAvps.removeAvp(Avp.DESTINATION_HOST);
		requestAvps.addAvp(Avp.DESTINATION_HOST,realmName,true,false,true);
		AvpSet mediaComponentDescriptionGroupe = requestAvps.addGroupedAvp(MEDIA_COMPONENT_DESCRIPTION, 10415, true,
				false);
		mediaComponentDescriptionGroupe.addAvp(MEDIA_COMPONENT_NUMBER, mediaDes.getMediaComponentNumber(), 10415,
				true, false, true);

		AvpSet mediaSubComponentGroupe1 = mediaComponentDescriptionGroupe.addGroupedAvp(MEDIA_SUB_COMPONENT, 10415,
				true, false);
		mediaSubComponentGroupe1.addAvp(FLOW_NUMBER, mediaDes.getMediaSubComponent().get(0).getFlowNumber(), 10415,
				true, false, true);
		mediaSubComponentGroupe1.addAvp(FLOW_DESCRIPTION,
				mediaDes.getMediaSubComponent().get(0).getFlowDescriptionOut(), 10415, true, false, true);
		mediaSubComponentGroupe1.addAvp(FLOW_DESCRIPTION,
				mediaDes.getMediaSubComponent().get(0).getFlowDescriptionIn(), 10415, true, false, true);

		
		mediaComponentDescriptionGroupe.addAvp(MEDIA_TYPE, mediaDes.getMediaType(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(FLOW_STATUS, mediaDes.getFlowStatus(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(CODEC_DATA, mediaDes.getCodecDataUl(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(CODEC_DATA, mediaDes.getCodecDataDl(), 10415, true, false, true);

		AvpSet subscriptionIdGroupe = requestAvps.addGroupedAvp(SUBSCRIPTION_ID, true, false);
		subscriptionIdGroupe.addAvp(SUBSCRIPTION_ID_TYPE, subscriptionIdType, true, false, true);
		subscriptionIdGroupe.addAvp(SUBSCRIPTION_ID_DATA, "200010001001126"/*"<sip:" + subscriptionId + ">"*/, true, false, true);

		// Remove
		// requestAvps.removeAvp(DESTINATION_HOST);

		// send
		try {
			session.send(request, this);
		} catch (InternalException | IllegalDiameterStateException | RouteException | OverloadException e1) {
			log.error("AAR Failed due to: "+e1);
			e1.printStackTrace();
		}
		dumpMessage(request, true); // dump info on console

		try {
			synchronized (AAA_WAIT) {
				AAA_Gotresult = false;
				AAA_WAIT.wait(10 * 1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "AAA Exception" + e.getMessage();
		}

		if(AAA_Gotresult == false) {
			AAAResultCode = "2001";
			log.error("DIdnt recive any AA result for: subscriptionId->" + subscriptionId + " will return 2001" );
		}
		
		return AAAResultCode;
	}

	private String sendAarMessage(MessageType messageType, Map<String, String> data,String port) {
		MediaComponentDescription mediaDes = new MediaComponentDescription();
		int subscriptionIdType = 0;
		this.config = ConfigurationUtils.loadDiamConfigFile();
		
		switch (messageType) {
		case CBR1:
			mediaDes = this.config.getCBR1().getMediaComponentDescription();
			subscriptionIdType = this.config.getCBR1().getSubscriptionIdType();
			break;

		case CBR2:
			mediaDes = this.config.getCBR2().getMediaComponentDescription();
			subscriptionIdType = this.config.getCBR2().getSubscriptionIdType();
			break;

		case MBR:
			mediaDes = this.config.getMBR().getMediaComponentDescription();
			subscriptionIdType = this.config.getMBR().getSubscriptionIdType();
			break;
		default:
			log.error("Diameter Client did not receive a message type");
		}
		
		String subscriptionId = data.get("MSISDN");
		Integer bandwidthUl = mediaDes.getMaxRequestedUl();
		Integer bandwidthDl = mediaDes.getMaxRequestedDl();
		
		if (messageType == MessageType.MBR && data.get("BANDWIDTH") != null) {
			bandwidthUl = (bandwidthUl == null) ? Integer.valueOf(data.get("BANDWIDTH")) : bandwidthUl;
			bandwidthDl = (bandwidthDl == null) ? Integer.valueOf(data.get("BANDWIDTH")) : bandwidthDl;
		}
		String serverHost = stack.getMetaData().getConfiguration().getChildren(37)[0].getChildren(23)[0].getStringValue(79,"").split(":")[0];
		String realmName = this.stack.getMetaData().getLocalPeer().getRealmName();
		Request request = session.createRequest(AA_COMMAND, this.App3gppRX, realmName,serverHost +":" + port);
		request.setRequest(true);
		request.setProxiable(true);
		AvpSet requestAvps = request.getAvps();
		
		requestAvps.removeAvp(Avp.DESTINATION_HOST);
		requestAvps.addAvp(Avp.DESTINATION_HOST,"pcrf2311aric.aricent.com",true,false,true);
		AvpSet mediaComponentDescriptionGroupe = requestAvps.addGroupedAvp(MEDIA_COMPONENT_DESCRIPTION, 10415, true,
				false);
		mediaComponentDescriptionGroupe.addAvp(MEDIA_COMPONENT_NUMBER, mediaDes.getMediaComponentNumber(), 10415,
				true, false, true);

		AvpSet mediaSubComponentGroupe1 = mediaComponentDescriptionGroupe.addGroupedAvp(MEDIA_SUB_COMPONENT, 10415,
				true, false);
		mediaSubComponentGroupe1.addAvp(FLOW_NUMBER, mediaDes.getMediaSubComponent().get(0).getFlowNumber(), 10415,
				true, false, true);
		mediaSubComponentGroupe1.addAvp(FLOW_DESCRIPTION,
				mediaDes.getMediaSubComponent().get(0).getFlowDescriptionOut(), 10415, true, false, true);
		mediaSubComponentGroupe1.addAvp(FLOW_DESCRIPTION,
				mediaDes.getMediaSubComponent().get(0).getFlowDescriptionIn(), 10415, true, false, true);

		AvpSet mediaSubComponentGroupe2 = mediaComponentDescriptionGroupe.addGroupedAvp(MEDIA_SUB_COMPONENT, 10415,
				true, false);
		mediaSubComponentGroupe2.addAvp(FLOW_NUMBER, mediaDes.getMediaSubComponent().get(1).getFlowNumber(), 10415,
				true, false, true);
		mediaSubComponentGroupe2.addAvp(FLOW_DESCRIPTION,
				mediaDes.getMediaSubComponent().get(1).getFlowDescriptionOut(), 10415, true, false, true);
		mediaSubComponentGroupe2.addAvp(FLOW_DESCRIPTION, mediaDes.getMediaSubComponent().get(1).getFlowDescriptionIn(),
				10415, true, false, true);
		mediaSubComponentGroupe2.addAvp(FLOW_USAGE, mediaDes.getMediaSubComponent().get(1).getFlowUsag(), 10415, true,
				false, true);

		mediaComponentDescriptionGroupe.addAvp(MEDIA_TYPE, mediaDes.getMediaType(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(MAX_REQUESTED_BANDWIDTH_UL, bandwidthUl, 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(MAX_REQUESTED_BANDWIDTH_DL, bandwidthDl, 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(FLOW_STATUS, mediaDes.getFlowStatus(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(CODEC_DATA, mediaDes.getCodecDataUl(), 10415, true, false, true);
		mediaComponentDescriptionGroupe.addAvp(CODEC_DATA, mediaDes.getCodecDataDl(), 10415, true, false, true);

		AvpSet subscriptionIdGroupe = requestAvps.addGroupedAvp(SUBSCRIPTION_ID, true, false);
		subscriptionIdGroupe.addAvp(SUBSCRIPTION_ID_TYPE, subscriptionIdType, true, false, true);
		subscriptionIdGroupe.addAvp(SUBSCRIPTION_ID_DATA, "200010001001126"/*"<sip:" + subscriptionId + ">"*/, true, false, true);

		// Remove
		// requestAvps.removeAvp(DESTINATION_HOST);

		// send
		try {
			session.send(request, this);
		} catch (InternalException | IllegalDiameterStateException | RouteException | OverloadException e1) {
			log.error("AAR Failed due to: "+e1);
			e1.printStackTrace();
		}
		dumpMessage(request, true); // dump info on console

		try {
			synchronized (AAA_WAIT) {
				AAA_Gotresult = false;
				AAA_WAIT.wait(10 * 1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return "AAA Exception" + e.getMessage();
		}

		if(AAA_Gotresult == false) {
			AAAResultCode = "2001";
			log.error("DIdnt recive any AA result for: subscriptionId->" + subscriptionId + " will return 2001" );
		}
		
		return AAAResultCode;
	}
	
	
	public String sendAarMessage(MessageType messageType, Map<String, String> data) {
		if(messageType == MessageType.CBR1)
			return sendD1AarMessage(data,"3868");
		return sendAarMessage(messageType, data,"3868");
	}

	public void addListener(DiameterListener listener) {
		this.listeners.add(listener);
	}

	public String getStatus() {
		String ret = "";

		ret += "Active: " + this.stack.isActive() + "\n";
		ret += "Session ID: " + this.session.getSessionId() + "\n";
		ret += "Session Last Accessed Time: " + new Date(this.session.getLastAccessedTime()) + "\n";
		ret += "Session Creation Time: " + new Date(this.session.getCreationTime()) + "\n";

		return ret;
	}

	

	private DiamConfig loadConfigFile(String location) {
		log.debug("loading config file " + location);
		DiamConfig diamConfig = new DiamConfig();
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(DiamConfig.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			diamConfig = (DiamConfig) jaxbUnmarshaller.unmarshal(new File(location));

		} catch (JAXBException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return diamConfig;
	}

}