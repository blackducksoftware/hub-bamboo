/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
