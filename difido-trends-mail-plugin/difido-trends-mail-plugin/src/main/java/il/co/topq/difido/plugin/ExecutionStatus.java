package il.co.topq.difido.plugin;

import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class ExecutionStatus {
	private final int id;
	private final String type;
	private final String targetVersion;
	private final int pass;
	private final int fail;
	private final int warning;
	private final String duration;

	public ExecutionStatus(int id, String type, String targetVersion, int pass, int fail, int warning, String duration) {
		super();
		this.id = id;
		this.type = type;
		this.targetVersion = targetVersion;
		this.pass = pass;
		this.fail = fail;
		this.warning = warning;
		this.duration = duration;
	}

	public ExecutionStatus(int id, String type) {
		super();
		this.id = id;
		this.type = type;
		this.targetVersion = "";
		this.pass = 0;
		this.fail = 0;
		this.warning = 0;
		this.duration = "";
	}

	public static ExecutionStatus caluculateExecutionStatuses(List<ElasticsearchTest> tests, int id, String type, String targetVersion) {
		int success = 0, failure = 0, warning = 0;
		long duration = 0;
		for (ElasticsearchTest test : tests) {
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
		return new ExecutionStatus(id, type, targetVersion, success, failure, warning, DurationFormatUtils.formatDuration(duration, "HH:mm:ss"));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id:");
		sb.append(id);
		sb.append(", type");
		sb.append(type);
		sb.append(", pass");
		sb.append(pass);
		sb.append(", fail");
		sb.append(fail);
		sb.append(", warning");
		sb.append(warning);
		sb.append(", duration");
		sb.append(duration);
		return sb.toString();
	}

	public int getId() {
		return id;
	}

	public String getType() {
		return type;
	}
	
	public String getTargetVersion() {
		return targetVersion;
	}

	public int getPass() {
		return pass;
	}

	public int getFail() {
		return fail;
	}

	public int getWarning() {
		return warning;
	}

	public String getDuration() {
		return duration;
	}
}