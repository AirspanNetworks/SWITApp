package il.co.topq.difido.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;

public class ShortSanityHandler {

	private static final String UNEXPECTED_REBOOTS = "Unexpected Reboots";
	private static final Logger log = LoggerFactory.getLogger(TrendsMailPlugin.class);
	private String subject = "";
	private String scenario;
	private String slave;
	private String branch;
	private String mailingList;
	private String version;
	private ExecutionMetadata metaData;
	private String versionPath;

	public ShortSanityHandler(ExecutionMetadata metaData) {
		this.metaData = metaData;
	}

	public void shortSanityHandler(boolean newRun) {
		log.info("Short sanity handler");

		if (!newRun) {
			log.info("Not a new run, not handeling short sanity.");
			return;
		}
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!checkAllTestsPerformed()) {
			log.error("Not all tests were performed.");
			return;
		}

		if (!getPropertiesValues()) {
			log.error("Properties are not valid.");
			return;
		}

		if (scenario.contains("ShortSanity") && mailingList.contains("Sanity")) {
			log.info("Run is relevent, checking tests success");
			if (automatedTriggerConditions())
			{
				String lastGoodVersionPath = new File(System.getProperty("user.dir")) + "/lastGoodVersion/";
				compareAndWriteVersionFlag(lastGoodVersionPath);
				String configFilePath = new File(System.getProperty("user.dir")) + "/runFlags/";
				compareAndWriteVersionFlag(configFilePath);
			}
			
		}
	}

	private boolean automatedTriggerConditions() {
		final int executionId = metaData.getId();
		List<ElasticsearchTest> allTests;
		Map<String, String> scenarioProperties;
		try {
			allTests = ExecutionUtils.getAllTestsOfExecution(executionId);
			scenarioProperties = ExecutionUtils.getExecutionScenarioProperties(executionId);
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error("getAllTestsOfExecutionSortedById Failed due to " + e1.getMessage());
			return false;
		}
		boolean swUpgradeFlag = false;
		boolean tptFlag = false;
		String logCounter = scenarioProperties.get("LogCounter");
		for (ElasticsearchTest test : allTests) {
			if (test.getName().contains("Software Upgrade") && (test.getStatus().equalsIgnoreCase("success") || test.getStatus().equalsIgnoreCase("warning"))) {
				log.info(test.getName() + " passed, software upgrade condition OK");
				swUpgradeFlag = true;
			}
			if (test.getName().contains("TPT") && (test.getStatus().equalsIgnoreCase("success") || test.getStatus().equalsIgnoreCase("warning"))) {
				log.info(test.getName() + " passed, Throughput condition OK");
				tptFlag = true;
			}
		}	
		boolean rebootCondition = getUnExpectedRebootCondition(logCounter);
		return swUpgradeFlag && tptFlag && rebootCondition;
	}

	public boolean getUnExpectedRebootCondition(String logCounter) {
		if(logCounter == null) {
			return true;
		}
		Map<String, String> lineExpressionKeys = ExecutionUtils.getAllLineExpressionKeys(logCounter);
		
		if (!lineExpressionKeys.isEmpty()) {			
			Map<String, Map<String, Map<String, String>>> allMaps = ExecutionUtils.getLineExpressionMapBySeverity(lineExpressionKeys); 
			for(String key1 : allMaps.keySet()) {
				for(String key2 : allMaps.get(key1).keySet()) {
					if(key2.contains(UNEXPECTED_REBOOTS)) {
						log.info("there are unexpected reboots, not making full sanity flag.");
						return false;
					}
				}
			}
		}
		log.info("there are no unexpected reboots, condition OK.");
		return true;
	}
	
	private void compareAndWriteVersionFlag(String folderPath) {
		log.info("checking in folder " + folderPath);
		File folder = new File(folderPath);
		folder.mkdirs();
		File[] folderFileList = folder.listFiles();
		boolean makeNewFile = true;
		for (final File fileEntry : folderFileList) {
			log.info("Checking file: " + fileEntry.getName());
			if (fileEntry.getName().equals(slave)) {
				makeNewFile = false;
				String fileVersionPath = getVersionPathFromFile(fileEntry);
				if (compareFoldersCreationDate(versionPath, fileVersionPath) > 0) {					
					log.info("This run is a newer version - " + version + ", creating file: " + slave);
					fileEntry.delete();
					try {
						createFile(folder);
					} catch (IOException e) {
						makeNewFile = true;
						e.printStackTrace();
					}

				} else {
					log.info(
							"This run is an equal or older version - " + version + ", not changing file: " + fileEntry);
				}
				break;
			} else {
				log.info("File not a match for current run on " + slave);
			}
		}

		if (makeNewFile) {
			log.info("This run is a new, creating file: " + slave);
			try {
				createFile(folder);
			} catch (IOException e) {
				log.error("Failed creating file: " + subject);
				e.printStackTrace();
			}
		}
	}

	private void createFile(File runFlagsFolder) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(runFlagsFolder + File.separator + slave, "UTF-8");
		writer.println(version);
		writer.println(slave);
		writer.println(branch);
		writer.println(mailingList);
		writer.println(versionPath);
		writer.println(scenario);
		writer.close();
	}

	private String getVersionPathFromFile(final File fileEntry) {
		String line = "";
		try {
			FileReader fileReader = new FileReader(fileEntry);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while((line = bufferedReader.readLine()) != null)
			{
				log.info("line : " + line);
				line = line.toLowerCase();
				String path = line.replace("\\\\fs4\\projects\\", "/mnt/P/");
				path = path.replace("\\", "/");
				log.info("path : " + path);
				File file = new File(path);
				if (file.isDirectory())
					break;
			}
			fileReader.close();
		} catch (Exception e) {
			return "";
		}
		if (line == null) {
			log.warn("Version Path not found!");
			line = "";
		}
		else
			log.info("Version Path found: " + line);
		return line;
	}

	private Boolean checkCompletePass() {// keep untill march 2018 becase of management decision changes, if still not used, delete.
		int successTests = metaData.getNumOfSuccessfulTests();
		int warningTests = metaData.getNumOfTestsWithWarnings();
		int failTests = metaData.getNumOfFailedTests();
		int numOfAllTests = metaData.getNumOfTests();
		log.info("successTests = " + successTests + ", warningTests = " + warningTests + ", failTests = " + failTests);
		if ((successTests + warningTests) != numOfAllTests) {
			log.info("Not relevent run because not complete pass");
			return false;
		}
		log.info("Relevent run because all tests passed");
		return true;
	}

	private Boolean getPropertiesValues() {
		Boolean validProporties = true;
		scenario = metaData.getProperties().get(TrendsMailPlugin.SCENARIO_NAME);
		if ((null == scenario) || scenario.isEmpty()) {
			log.error("Execution Scenario of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		} else {
			subject += scenario;
		}
		slave = metaData.getProperties().get(TrendsMailPlugin.SETUP_NAME);
		if (null == slave || slave.isEmpty()) {
			log.error("SetupName of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		} else {
			subject += "-" + slave;
		}
		branch = metaData.getProperties().get(TrendsMailPlugin.BRANCH);
		if ((null == branch) || branch.isEmpty()) {
			log.error("Branch of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		} else {
			subject += "-" + branch;
		}
		mailingList = metaData.getProperties().get(TrendsMailPlugin.MAILING_LIST);
		if ((null == mailingList) || mailingList.isEmpty()) {
			log.error("mailingList of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		} else {
			subject += "-" + mailingList;
		}
		version = metaData.getProperties().get(TrendsMailPlugin.TARGET_VERSION);
		if ((null == version) || version.isEmpty()) {
			log.error("targetVersion of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		} else {
			subject += "-" + version;
		}
		versionPath = metaData.getProperties().get(TrendsMailPlugin.VERSION_PATH);
		if ((null == version) || version.isEmpty()) {
			log.error("targetVersion of execution with id " + metaData.getId() + " is missing");
			validProporties = false;
		}
		log.info("Subject: " + subject);
		return validProporties;
	}

	private Boolean checkAllTestsPerformed() {
		log.debug("geting execution id");
		final int executionId = metaData.getId();
		log.info("Checking run with id " + executionId);
		log.debug("geting tests of execution");
		List<ElasticsearchTest> sortedTests;
		List<ElasticsearchTest> allTests;
		try {
			allTests = ExecutionUtils.getAllTestsOfExecution(executionId);
			sortedTests = ExecutionUtils.getAllTestsOfExecutionSortedById(allTests, executionId);
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error("getAllTestsOfExecutionSortedById Failed due to " + e1.getMessage());
			log.warn("Wrong mail will send to the second mailing list");
			return false;
		}

		log.debug("calling sendMailCheck method");
		if (metaData.getExecution() != null) {
			try {
				int plannedTests = metaData.getExecution().getLastMachine().getPlannedTests();
				if (plannedTests > sortedTests.size()) {
					log.warn("Not all tests in execution with id " + metaData.getId() + " were executed");
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
	}
	
	private int compareFoldersCreationDate(String folder1, String folder2) {
		log.info("Recieved versionFolder1: " + folder1);
		log.info("Recieved versionFolder2: " + folder2);		
		folder1 = folder1.toLowerCase().replace("\\\\fs4\\projects\\", "/mnt/P/");
		folder1 = folder1.replace("\\", "/");
		folder2 = folder2.toLowerCase().replace("\\\\fs4\\projects\\", "/mnt/P/");
		folder2 = folder2.replace("\\", "/");
		log.info("after versionFolder1: " + folder1);
		log.info("after versionFolder2: " + folder2);
		try {
			File folderFile1 = new File(folder1);
			if(!folderFile1.exists())
				return 1;
			
			File folderFile2 = new File(folder2);
			if(!folderFile2.exists())
				return -1;
			
			Path folderPath1 = Paths.get(folder1);			
			BasicFileAttributes folderAttr1 = Files.readAttributes(folderPath1, BasicFileAttributes.class);
			Path folderPath2 = Paths.get(folder2);
			BasicFileAttributes folderAttr2 = Files.readAttributes(folderPath2, BasicFileAttributes.class);
			return folderAttr1.creationTime().compareTo(folderAttr2.creationTime());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return -1;
	}

}
