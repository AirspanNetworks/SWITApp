package il.co.topq.difido.plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.business.elastic.ElasticsearchTest;
import il.co.topq.report.business.execution.ExecutionMetadata;

import static il.co.topq.difido.plugin.TrendsMailPlugin.MAILING_LIST;
import static il.co.topq.difido.plugin.TrendsMailPlugin.SEND_MAIL_FLAG;
import static il.co.topq.difido.plugin.TrendsMailPlugin.SETUP_NAME;

public class MailUtils {
	private static final Logger log = LoggerFactory.getLogger(MailUtils.class);

	public static boolean sendMailCheck(Map<String, String> scenarioProperties,
			List<ElasticsearchTest> tests, int executionId) {
		log.debug("calling sendMailCheck method");
		boolean response = true;
		
		if (tests.size() > 0){
			for (ElasticsearchTest elasticsearchTest : tests) {
				log.info("Test " + elasticsearchTest.getName() + " of execution with id " + executionId + " duration is " + elasticsearchTest.getDuration());
				if (elasticsearchTest.getDuration() == 0){
					log.warn("Test " + elasticsearchTest.getName() + " of execution with id " + executionId + " duration is 0, did not finish correctly.");
					response = false;
				}
			}			
		}
		else {
			log.warn("Execution with id " + executionId + " has no tests.");
			response = false;
		}
			

		if ((scenarioProperties.get(SETUP_NAME) == null)
				|| (scenarioProperties.get(SETUP_NAME).isEmpty())) {
			log.warn("Execution setupName of execution with id " + executionId + " is missing");
			response = false;
		}

		if (scenarioProperties.get(MAILING_LIST) == null
				|| scenarioProperties.get(MAILING_LIST).isEmpty()) {
			log.warn("Execution MailingList property of execution with id " + executionId + " is missing");
			response = false;
		}

		if (scenarioProperties.get(SEND_MAIL_FLAG) == null
				|| scenarioProperties.get(SEND_MAIL_FLAG).isEmpty()) {
			log.warn("SendMailFlag of execution with id " + executionId + " is missing");
			response = false;
		}

		if (!scenarioProperties.get(SEND_MAIL_FLAG).equals("true")) {
			log.warn("SendMailFlag: " + scenarioProperties.get(SEND_MAIL_FLAG));
			response = false;
		}

		return response;
	}

	public static void saveMailToVersionFolder(ExecutionMetadata metadata, StringBuilder body, String fileName) {

		String scenario = metadata.getProperties().get(TrendsMailPlugin.SCENARIO_NAME);
		if ((null == scenario) || scenario.isEmpty()) {
			log.error("Execution Scenario of execution with id " + metadata.getId() + " is missing");
			return;
		}

		if (!scenario.toLowerCase().contains("sanity")) {
			log.info("scenario of execution with id " + metadata.getId()
					+ " is not Sanity, not saving file in version folder.");
			return;
		} else
			log.info("scenario of execution with id " + metadata.getId() + " is " + scenario);

		String mailingList = metadata.getProperties().get(TrendsMailPlugin.MAILING_LIST);
		if ((null == mailingList)) {
			log.error("mailingList of execution with id " + metadata.getId() + " is missing");
			return;
		}

		if (!mailingList.equals("Sanity")) {
			log.info("mailingList of execution with id " + metadata.getId()
					+ " is not Sanity, not saving file in version folder.");
			return;
		} else
			log.info("mailingList of execution with id " + metadata.getId() + " is Sanity.");
		String versionFolder = metadata.getProperties().get(TrendsMailPlugin.VERSION_PATH);
		if ((null == versionFolder)) {
			log.error("versionFolder of execution with id " + metadata.getId() + " is missing");
			return;
		} else
			log.info("versionFolder of execution with id " + metadata.getId() + " is " + versionFolder);

		log.info("MailSubject of execution with id " + metadata.getId() + " is " + fileName);
		saveMail(versionFolder, fileName, body.toString());

	}

	private static void saveMail(String versionFolder, String fileName, String body) {

		log.info("Recieved versionFolder: " + versionFolder);
		log.info("Recieved fileName: " + fileName);

		fileName = fileName.replace("/", "_");
		fileName = fileName.replace("\\", "_");
		fileName = fileName.replace(" ", "");
		fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
		
		log.info("legal fileName: " + fileName);
		
		versionFolder = versionFolder.replace("\\\\FS4\\Projects\\", "/mnt/P/");
		versionFolder = versionFolder.replace("\\", "/");

		File releaseDir = new File(versionFolder);
		log.info("releaseDir: " + releaseDir.getAbsolutePath());
		File testsDir = new File(releaseDir.getParent().toString() + "/tests");
		log.info("testsDir: " + testsDir.getAbsolutePath());
		if (!testsDir.exists()) {
			log.info("creating directory: " + testsDir.getAbsolutePath());
			try {
				testsDir.mkdirs();
				log.info("DIR created");
			} catch (Exception e) {
				e.printStackTrace();
				log.error("Failed to create dir " + testsDir.getAbsolutePath());
				return;
			}
		}

		BufferedWriter oFile = null;
		File yourFile = null;
		try {

			File[] listOfFiles = testsDir.listFiles();
			int counter = 0;
			for (File file : listOfFiles) {
				if (file.isFile() && file.getName().contains(fileName))
					counter++;
			}

			if (counter > 0) {
				fileName += "_" + counter;
			}
			fileName += ".html";

			yourFile = new File(testsDir.getAbsolutePath() + "/" + fileName);
			log.info("Writing file:" + yourFile.getAbsolutePath());

			yourFile.createNewFile();
			oFile = new BufferedWriter(new FileWriter(yourFile));
			oFile.write(body);

		} catch (Exception e) {
			log.error("Failed writing file");
			e.printStackTrace();
		} finally {
			if (oFile != null) {
				try {
					oFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
