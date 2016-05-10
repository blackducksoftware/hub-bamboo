/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.bamboo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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

	public void setLogLevel(final Map<String, String> envVars) {
		final String logLevel = HubBambooUtils.getInstance().getEnvironmentVariable(envVars, "HUB_LOG_LEVEL", true);
		try {
			if (StringUtils.isNotBlank(logLevel)) {
				setLogLevel(LogLevel.valueOf(logLevel.toUpperCase()));
			} else {
				setLogLevel(LogLevel.INFO);
			}
		} catch (final IllegalArgumentException e) {
			setLogLevel(LogLevel.INFO);
		}
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
		if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
			logThrowable(txt, throwable);
		}
	}

	public void error(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
			logErrorMessage(txt);
		}
	}

	public void error(final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
			logThrowable(throwable);
		}
	}

	public void info(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.INFO)) {
			logMessage(txt);
		}
	}

	public void trace(final String txt, final Throwable throwable) {
		if (LogLevel.isLoggable(level, LogLevel.TRACE)) {
			logThrowable(txt, throwable);
		}
	}

	public void trace(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.TRACE)) {
			logMessage(txt);
		}
	}

	public void warn(final String txt) {
		if (LogLevel.isLoggable(level, LogLevel.WARN)) {
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
