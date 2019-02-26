package com.swit;

import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class FeatureStatus extends ExecutionStatus {

	public FeatureStatus(int id, String type, String targetVersion, int pass, int fail, int warning, String duration) {
		super(id, type, targetVersion, pass, fail, warning, duration);
	}
	
	public FeatureStatus(int id, String type) {
		super(id, type);
	}
	
	public static FeatureStatus caluculateStatusesPerFeature(List<ElasticsearchTest> tests, int id,
			String featureType, String targetVersion) {
		int success = 0, failure = 0, warning = 0;
		long duration = 0;
		for (ElasticsearchTest test : tests) {
			String[] parts = test.getProperties().get("Class").split("\\.");
			if (parts[2] != null && parts[2].equals(featureType)) {
				switch (test.getStatus()) {
				case "success":
					success++;
					break;
				case "failure":
				case "error":
					failure++;
					break;
				case "warning":
					warning++;
					break;
				}
				duration += test.getDuration();
			}
		}
		return new FeatureStatus(id, featureType, targetVersion, success, failure, warning, DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
	}
}
