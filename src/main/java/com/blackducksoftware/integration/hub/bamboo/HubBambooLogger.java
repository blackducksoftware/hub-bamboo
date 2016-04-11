package com.blackducksoftware.integration.hub.bamboo;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class HubBambooLogger implements IntLogger {

	private final BuildLogger buildLogger;
	private LogLevel level = LogLevel.INFO;

	public HubBambooLogger(final BuildLogger buildLogger) {
		this.buildLogger = buildLogger;
	}

	public BuildLogger getBuildLogger() {
		return buildLogger;
	}

	public LogLevel getLogLevel() {
		return level;
	}

	public void setLogLevel(final LogLevel level) {
		this.level = level;
	}

	public void debug(final String txt, final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logThrowable(txt, throwable);
		}
	}

	public void debug(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logMessage(txt);
		}
	}

	public void error(final String txt, final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logThrowable(txt, throwable);
		}
	}

	public void error(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logErrorMessage(txt);
		}
	}

	public void error(final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logThrowable(throwable);
		}
	}

	public void info(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logMessage(txt);
		}
	}

	public void trace(final String txt, final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logThrowable(txt, throwable);
		}
	}

	public void trace(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logMessage(txt);
		}
	}

	public void warn(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
			logMessage(txt);
		}
	}

	private void logMessage(final String txt) {
		if (txt != null) {
			if (buildLogger != null) {
				buildLogger.addBuildLogEntry(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logErrorMessage(final String txt) {
		if (txt != null) {
			if (buildLogger != null) {
				buildLogger.addErrorLogEntry(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logThrowable(final Throwable throwable) {
		logThrowable("An error occurred caused by ", throwable);
	}

	private void logThrowable(final String txt, final Throwable throwable) {
		if (txt != null) {
			if (buildLogger != null) {
				buildLogger.addErrorLogEntry(txt, throwable);
			} else {
				final StringWriter sw = new StringWriter();
				throwable.printStackTrace(new PrintWriter(sw));
				System.err.println(sw.toString());
			}
		}
	}
}
