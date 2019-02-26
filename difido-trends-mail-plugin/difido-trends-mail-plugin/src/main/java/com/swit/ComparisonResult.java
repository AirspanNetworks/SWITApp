package com.swit;

import java.util.ArrayList;
import java.util.List;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.report.business.elastic.ElasticsearchTest;

public class ComparisonResult {

	private final int passToFail;
	private final int passToWarning;
	private final int failToPass;
	private final int failToWarning;
	private final int warningToPass;
	private final int warningToFail;
	private final int newTests;
	private final int deleteTests;

	static public ComparisonResult compareExecutions(List<ElasticsearchTest> currentTests,
			List<ElasticsearchTest> previousTests) {
		int passToFail = 0, passToWarning = 0, failToPass = 0, failToWarning = 0, warningToPass = 0, warningToFail = 0,
				newTests = 0, deleteTests = 0;
		ElasticsearchTest saveTest = null;
		boolean flagSameTests = false;

		List<ElasticsearchTest> currentTestsCopy = new ArrayList<ElasticsearchTest>();
		List<ElasticsearchTest> previousTestsCopy = new ArrayList<ElasticsearchTest>();
		currentTestsCopy.addAll(currentTests);
		previousTestsCopy.addAll(previousTests);

		// Count how many new tests created
		for (ElasticsearchTest currentTest : currentTestsCopy) {
			flagSameTests = false;
			for (ElasticsearchTest prevTest : previousTestsCopy) {
				if (TestUtils.isTestsEquals(currentTest, prevTest)) {
					flagSameTests = true;
					saveTest = prevTest;
				}
			}
			if (!flagSameTests) {
				newTests++;
			}
			if (saveTest != null) {
				previousTestsCopy.remove(saveTest);
				saveTest = null;
			}
		}
		previousTestsCopy.clear();
		previousTestsCopy.addAll(previousTests);

		// Count how many tests have been deleted
		for (ElasticsearchTest prevTest : previousTestsCopy) {
			flagSameTests = false;
			for (ElasticsearchTest currentTest : currentTestsCopy) {
				if (TestUtils.isTestsEquals(currentTest, prevTest)) {
					flagSameTests = true;
					saveTest = currentTest;
				}
			}
			if (!flagSameTests) {
				deleteTests++;
			}
			if (saveTest != null) {
				currentTestsCopy.remove(saveTest);
				saveTest = null;
			}
		}
		currentTestsCopy.clear();
		currentTestsCopy.addAll(currentTests);

		for (ElasticsearchTest currentTest : currentTestsCopy) {
			for (ElasticsearchTest prevTest : previousTestsCopy) {
				if (TestUtils.isTestsEquals(currentTest, prevTest)) {
					if ((prevTest.getStatus().equals(Status.success.name()))
							&& ((currentTest.getStatus().equals(Status.error.name()))
									|| currentTest.getStatus().equals(Status.failure.name()))) {
						passToFail++;
					} else if ((prevTest.getStatus().equals(Status.success.name()))
							&& (currentTest.getStatus().equals(Status.warning.name()))) {
						passToWarning++;
					} else if ((prevTest.getStatus().equals(Status.failure.name())
							|| (prevTest.getStatus().equals(Status.error.name())))
							&& (currentTest.getStatus().equals(Status.success.name()))) {
						failToPass++;
					} else if ((prevTest.getStatus().equals(Status.failure.name())
							|| (prevTest.getStatus().equals(Status.error.name())))
							&& (currentTest.getStatus().equals(Status.warning.name()))) {
						failToWarning++;
					} else if ((prevTest.getStatus().equals(Status.warning.name()))
							&& (currentTest.getStatus().equals(Status.success.name()))) {
						warningToPass++;
					} else if ((prevTest.getStatus().equals(Status.warning.name()))
							&& (currentTest.getStatus().equals(Status.failure.name())
									|| (currentTest.getStatus().equals(Status.error.name())))) {
						warningToFail++;
					}
				}
				saveTest = prevTest;
			}
			if (saveTest != null) {
				previousTestsCopy.remove(saveTest);
				saveTest = null;
			}
		}
		return (new ComparisonResult(passToFail, passToWarning, failToPass, failToWarning, warningToPass, warningToFail,
				newTests, deleteTests));
	}

	public ComparisonResult() {
		this.passToFail = 0;
		this.passToWarning = 0;
		this.failToPass = 0;
		this.failToWarning = 0;
		this.warningToPass = 0;
		this.warningToFail = 0;
		this.newTests = 0;
		this.deleteTests = 0;
	}

	public ComparisonResult(int passToFail, int passToWarning, int failToPass, int failToWarning, int warningToPass,
			int warningToFail, int newTests, int deleteTests) {
		super();
		this.passToFail = passToFail;
		this.passToWarning = passToWarning;
		this.failToPass = failToPass;
		this.failToWarning = failToWarning;
		this.warningToPass = warningToPass;
		this.warningToFail = warningToFail;
		this.newTests = newTests;
		this.deleteTests = deleteTests;
	}

	public int getPassToFail() {
		return passToFail;
	}

	public int getPassToWarning() {
		return passToWarning;
	}

	public int getFailToPass() {
		return failToPass;
	}

	public int getFailToWarning() {
		return failToWarning;
	}

	public int getWarningToPass() {
		return warningToPass;
	}

	public int getWarningToFail() {
		return warningToFail;
	}

	public int getNewTests() {
		return newTests;
	}

	public int getDeleteTests() {
		return deleteTests;
	}
}
