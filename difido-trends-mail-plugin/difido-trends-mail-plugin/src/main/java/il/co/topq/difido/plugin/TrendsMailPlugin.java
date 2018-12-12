package il.co.topq.difido.plugin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;
import il.co.topq.report.plugins.mail.DefaultMailPlugin;

/**
 * 
 * @author Itai Agmon
 *
 */
public class TrendsMailPlugin extends DefaultMailPlugin {
	public static final String EXECUTION_TYPE = "Type";
	public static final String SCENARIO_NAME = "Scenario";
	public static final String CUSTOMER = "Customer";
	public static final String AUTOMATION_VERSION = "Version";
	public static final String BRANCH = "Branch";
	public static final String VERSION_PATH = "VersionPath";
	public static final String SETUP_NAME = "SetupName";
	public static final String DESCRIPTION = "Description";
	public static final String TARGET_VERSION = "targetVersion";
	public static final String SEND_MAIL_FLAG = "SendMailFlag";
	public static final String MAILING_LIST = "MailingList";
	public static final String SECOND_MAILING_LIST = "SpamList";
	public static final String START_TIME_DATE = "Date";
	
	//Setup Configuration Table
	public static final String ENB_TYPE = "EnbType";
	public static final String RELAY_VERSION = "RelayVersions";
	public static final String UE_TYPE = "UeType";
	public static final String NETSPAN_VERSION = "NetspanVar";
	public static final String SYSTEM_DEFAULTS_PROFILES = "SystemDefaultsProfiles";
	public static final String RADIO_PROFILES = "RadioProfiles";
	public static final String MOBILITY_PROFILES = "MobilityProfiles";
	public static final String ENB_ADVANCED_CONFIGURATION_PROFILES = "EnbAdvancedConfigurationProfiles";
	public static final String CELL_ADVANCED_CONFIGURATION_PROFILES = "CellAdvancedConfigurationProfiles";
	public static final String CA_CONFIGURATION = "CaConfiguration";
	public static final String USING_DONOR_ENB = "UsingDonorEnb";
	public static final String IPSEC_STATUS = "IPSecStatus";
	public static final String PNP_STATUS = "pnpStatus";
	public static final String IP_VERSION = "IPVersion";
	
	public boolean problemCreatingMail = false;
	private static final Logger log = LoggerFactory.getLogger(TrendsMailPlugin.class);
	boolean newExecution = true;
	private String sendToAddress = "";
	protected String subject = "";

	@Override
	public String getName() {
		return "trendsMailPlugin";
	}

	public void onExecutionEnded(ExecutionMetadata metadata) {
		log.info("onExecutionEnded");
		log.info("isEnabled = " + isEnabled());
		try
		{
			sendExecutionMail(metadata);
		}
		catch(Throwable e){
			System.out.println("Error in DefaultMailPlugin!");
			e.printStackTrace();
		}
	}

	@Override
	public void execute(List<ExecutionMetadata> metaDataList, String params) {
		log.debug("calling execute method");
		this.sendToAddress = params;
		if(params.toLowerCase().equals("original"))
			this.newExecution = true;
		else
			this.newExecution = false;
		try
		{
			super.execute(metaDataList, params);
		}
		catch(Throwable e){
			System.out.println("Error in DefaultMailPlugin!");
			e.printStackTrace();
		}
		
	}

	@Override
	protected String[] getAttachments() {
		log.debug("calling getAttachments method");
		String fileLocation = null;
		try {
			int executionId = getMetadata().getId();
			List<ElasticsearchTest> allTests = ExecutionUtils.getAllTestsOfExecution(executionId);
			List<ElasticsearchTest> sortedTests = ExecutionUtils.getAllTestsOfExecutionSortedById(allTests, executionId);
			Map<String, String> scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(executionId);
			fileLocation = CSVUtils.createCSVFile(scenarioProperties, sortedTests, getMetadata().getId());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Create CSV File Failed due to " + e.getMessage());
			return null;
		}
		return new String[] { fileLocation };
	}

	@Override
	protected String getToAddress() {
		log.debug("calling getToAddress method");
		String toAddresses = "";

		try {// We need to give time to the Elastic to index the data
			Thread.sleep(5000);
			final int executionId = getMetadata().getId();
			final Map<String, String> scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(executionId);
			Configuration config = Configuration.getInstance(Configuration.MAIL_CONFIG_FILE_NAME);
			if (this.newExecution && this.problemCreatingMail) {
				toAddresses = config.readString(scenarioProperties.get(SECOND_MAILING_LIST));
			}

			else if (this.newExecution && !this.problemCreatingMail) {
				toAddresses = config.readString(scenarioProperties.get(MAILING_LIST));
			}

			else {// execute
				if(!this.sendToAddress.contains("@"))
					toAddresses = config.readString(this.sendToAddress);
				else
					toAddresses = this.sendToAddress;
			}
			log.debug("The address(es) to send the mail are: " + toAddresses);
		} catch (Exception e) {
			log.error("getToAddress Failed due to " + e.getMessage());
			this.problemCreatingMail = true;
		}
		return toAddresses;
	}

	@Override
	protected String getMailSubject() {
		// Format: {ScenarioName} - {SetupName}
		
		log.debug("calling getMailSubject method");
		subject = "";
		if (null == getMetadata() || null == getMetadata().getProperties()) {
			return subject;
		}
		
		Map<String, String> metaProperties = getMetadata().getProperties();
		try {		
			Map<String, String> scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(getMetadata().getId());
		
			if (this.problemCreatingMail && this.newExecution) {
				subject += "Incomplete - ";
			}
			log.info("CUSTOMER key is: " + CUSTOMER);
			if ((scenarioProperties.get(CUSTOMER) == null)
					|| (scenarioProperties.get(CUSTOMER).isEmpty())) {
				log.info("Execution with id " + getMetadata().getId() + " has no costumer.");
			} else {
				log.info("Execution with id " + getMetadata().getId() + " costumer is " + scenarioProperties.get(CUSTOMER));
				subject += scenarioProperties.get(CUSTOMER) + " ";
			}
		} catch (Exception e1) {
			log.error("Failed getting Costumer info.");
			e1.printStackTrace();
		}
		if ((null == metaProperties.get(SCENARIO_NAME))
				|| (metaProperties.get(SCENARIO_NAME)).isEmpty()) {
			log.warn("Execution Scenario of execution with id " + getMetadata().getId() + " is missing");
		} else {
			String scenarioName = metaProperties.get(SCENARIO_NAME);
			String scenarioNameWithSpace = scenarioName.replaceAll("(.)([A-Z])", "$1 $2");
			subject += scenarioNameWithSpace +": ";
		}
		if (null == (metaProperties.get(SETUP_NAME))
				|| (metaProperties.get(SETUP_NAME)).isEmpty()) {
			log.warn("SetupName of execution with id " + getMetadata().getId() + " is missing");
		} else {
			subject += metaProperties.get(SETUP_NAME);
		}
		
		if (null == (metaProperties.get(TARGET_VERSION))
				|| (metaProperties.get(TARGET_VERSION)).isEmpty()) {
			log.warn("target version of execution with id " + getMetadata().getId() + " is missing");
		} else {
			subject += " - " + metaProperties.get(TARGET_VERSION);
		}
		
		try {
			subject += " - "+ExecutionUtils.getStatus(getMetadata(), ExecutionUtils.getAllTestsOfExecution(getMetadata().getId()));
		} catch (Exception e) {
			e.printStackTrace();
			this.problemCreatingMail = true;
		}
		
		return subject;
	}

	@Override
	protected String getMailBody() {
		final StringBuilder body = new StringBuilder();
		try {
			// We need to give time to the Elastic to index the data
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			log.error("Thread.sleep Failed due to " + e1.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
			this.problemCreatingMail = true;
		}

		if (null == getMetadata()) {
			log.error("Metadata object is null");
			this.problemCreatingMail = true;
			return "Metadata object is null";
		}

		ExecutionMetadata metadata = getMetadata();
		try{
			ShortSanityHandler SanityHandler = new ShortSanityHandler(metadata);
			SanityHandler.shortSanityHandler(this.newExecution);
		}catch(Throwable t){
			log.error("Failed short sanity check. " + t.getMessage());
		}
		int executionId = metadata.getId();
		List<ElasticsearchTest> allTests;
		Map<String, String> scenarioProperties;
		try {
			allTests = ExecutionUtils.getAllTestsOfExecution(executionId);
			scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(executionId);
		} catch (Exception e) {
			log.error("Exception due to: " + e.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
			this.problemCreatingMail = true;
			return "Failed to create mail";
		}
		final String executionType = scenarioProperties.get(EXECUTION_TYPE);
		final String targetVersion = scenarioProperties.get(TARGET_VERSION);
		
		List<ElasticsearchTest> sortedTests = ExecutionUtils.getAllTestsOfExecutionSortedById(allTests, executionId);
		ExecutionStatus currentExecutionStatus = ExecutionStatus.caluculateExecutionStatuses(allTests, executionId,
				executionType, targetVersion);

		if (!MailUtils.sendMailCheck(scenarioProperties, sortedTests, executionId)) {
			log.error("Failure in creating correct mail");
			log.warn("Wrong mail will send to the second mailing list");
			this.problemCreatingMail = true;
		}
		
		body.append(MailBodyUtils.getTitle(metadata, scenarioProperties, allTests));
		body.append(MailBodyUtils.getBodyVersionsAndLinks(scenarioProperties, sortedTests, getMetadata().getUri(), currentExecutionStatus.getDuration()));
		//body.append(MailBodyUtils.getSummary(currentExecutionStatus, sortedTests, getMetadata().getUri()));
		body.append(MailBodyUtils.getTestsFearuresTable(allTests, executionId, executionType, targetVersion));
		body.append(MailBodyUtils.getTestsTable(sortedTests));
		
		try {
			body.append(MailBodyUtils.getComparisonBetweenTwoExecutions(currentExecutionStatus, executionId,
					executionType));
		} catch (Exception e) {
			log.error("Comparison Between Two Executions Failed due to: " + e.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
			this.problemCreatingMail = true;
			return "";
		}
		if(scenarioProperties.get("LogCounter") != null) {
			body.append(MailBodyUtils.getErrorsAndWarningsTable(scenarioProperties.get("LogCounter")));
		}
		else{
			body.append(MailBodyUtils.getErrorsAndWarningsTableOldFormat(scenarioProperties));
		}
		body.append(MailBodyUtils.getSetupConfigurationTable(scenarioProperties));
		body.append(MailBodyUtils.getGraphs(sortedTests));

		CSVUtils.createCSVFile(scenarioProperties, sortedTests, executionId);

		if (!this.problemCreatingMail && this.newExecution) {
			String fileName = subject.isEmpty() ? getMailSubject() : subject;
			MailUtils.saveMailToVersionFolder(getMetadata(), body, fileName);
		}

		return body.toString();
	}
}