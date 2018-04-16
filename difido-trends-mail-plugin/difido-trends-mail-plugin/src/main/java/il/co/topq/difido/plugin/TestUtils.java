package il.co.topq.difido.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class TestUtils {
	/**
	 * @param Returns
	 *            true if and only if this test contains key that contains
	 *            specified sequence of char values.
	 * @param s
	 *            the sequence to search for
	 * @return true if exists key that contains s, false otherwise
	 */
	public static boolean containsKey(ElasticsearchTest test, CharSequence s) {
		for (String key : test.getProperties().keySet()) {
			if (key.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTestsEquals(ElasticsearchTest test1, ElasticsearchTest test2) {
		if ((test1 == null) || (test2 == null)) {
			return false;
		}
		if (!test1.getName().equals(test2.getName())) {
			return false;
		}
		if ((test1.getParameters() == null) && (test2.getParameters() != null)) {
			return false;
		}
		if ((test1.getParameters() != null) && (test2.getParameters() == null)) {
			return false;
		}
		if ((test1.getParameters() != null) && (test2.getParameters() != null)) {
			if (test1.getParameters().size() != test2.getParameters().size()) {
				return false;
			}
			for (String key : test1.getParameters().keySet()) {
				if (key.equals("Status")) {
					continue;
				}
				if (!StringUtils.equals(test1.getParameters().get(key), test2.getParameters().get(key))) {
					return false;
				}
			}
		}
		return true;
	}

	public static List<String> getAllEnbsOfTest(ElasticsearchTest test) {
		List<String> enbs = new ArrayList<String>();

		for (String key : test.getProperties().keySet()) {
			if (key.contains("_Version") && key.split("_")[0].split("-").length > 1) {
				enbs.add(key.split("_")[0].split("-")[1]);
			}
		}
		return enbs;
	}
}
