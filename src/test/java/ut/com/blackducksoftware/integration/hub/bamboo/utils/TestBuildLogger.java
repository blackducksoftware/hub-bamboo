package ut.com.blackducksoftware.integration.hub.bamboo.utils;

import java.util.List;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.blackducksoftware.integration.hub.logging.IntBufferedLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class TestBuildLogger implements BuildLogger {

	private final IntBufferedLogger logger = new IntBufferedLogger();

	public String addBuildLogEntry(final LogEntry arg0) {
		return null;
	}

	public String addBuildLogEntry(final String arg0) {
		logger.info(arg0);
		return null;
	}

	public String addBuildLogHeader(final String arg0, final boolean arg1) {
		return null;
	}

	public String addErrorLogEntry(final LogEntry arg0) {
		return null;
	}

	public String addErrorLogEntry(final String arg0) {
		logger.error(arg0);
		return null;
	}

	public void addErrorLogEntry(final String arg0, final Throwable arg1) {
		logger.error(arg0, arg1);
	}

	public void clearBuildLog() {
		logger.resetAllLogs();
	}

	public void close() {
		logger.resetAllLogs();
	}

	public List<LogEntry> getBuildLog() {
		return null;
	}

	public List<LogEntry> getErrorLog() {
		return null;
	}

	public LogInterceptorStack getInterceptorStack() {
		return null;
	}

	public List<LogEntry> getLastNLogEntries(final int arg0) {
		return null;
	}

	public List<String> getStringBuildLogs() {
		return logger.getOutputList(LogLevel.INFO);
	}

	public List<String> getStringErrorLogs() {
		return logger.getOutputList(LogLevel.ERROR);
	}

	public long getTimeOfLastLog() {
		return 0;
	}

	public void startStreamingBuildLogs(final PlanResultKey arg0) {

	}

	public void stopStreamingBuildLogs() {

	}
}
