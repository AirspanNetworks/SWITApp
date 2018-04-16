package il.co.topq.difido.plugin;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;

import il.co.topq.report.business.elastic.ElasticsearchTest;

import static il.co.topq.difido.plugin.TrendsMailPlugin.SCENARIO_NAME;

public class CSVUtils {

	private static String CSV_FOLDER_LOCATION = "/home/swit/difido-server/CSV/";
	private static final Logger log = LoggerFactory.getLogger(CSVUtils.class);

	public static String createCSVFile(Map<String, String> scenarioProperties, List<ElasticsearchTest> tests,
			int executionId) {
		String scenarioName = scenarioProperties.get(SCENARIO_NAME);
		String scenarioNameWithSpace = scenarioName.replaceAll("(.)([A-Z])", "$1 $2");
		int index = 1;
		String fileName = scenarioNameWithSpace + " execution" + executionId + ".csv";
		try {
			FileWriter writer = new FileWriter(CSV_FOLDER_LOCATION + fileName);
			CSVWriter csvWriter = new CSVWriter(writer, ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER);
			List<String> lst = Arrays.asList("Index", "Start Time", "Test Domain", "Feature", "Test Name", "Priority",
					"Test Suite", "Status", "Duration", "Warning", "Link");
			csvWriter.writeNext((String[])lst.toArray());
			for (ElasticsearchTest test : tests) {
				String[] parts = { "", "", "", "" };
				parts = test.getProperties().get("Class").split("\\.");
				String part1 = parts.length >= 2 ? parts[1] : "";
				String part2 = parts.length >= 3 ? parts[2] : "";
				String part3 = parts.length >= 4 ? parts[3] : "";
				long duration = test.getDuration();
				String status = test.getStatus();
				String warning = "No";
				if (status.equalsIgnoreCase("warning")) {
					warning = "Yes";
					status = "success";
				}
				lst = Arrays.asList(String.valueOf(index++), test.getTimeStamp(), part1, part2, test.getName(), part3,
						scenarioName, status, DurationFormatUtils.formatDuration(duration, "HH:mm:ss"), warning, "=HYPERLINK(\"" + test.getUrl() + "\")");
				csvWriter.writeNext((String[])lst.toArray());
			}
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			log.debug("Failed to create CSV file");
			e.printStackTrace();
		}

		return CSV_FOLDER_LOCATION + fileName;
	}
}