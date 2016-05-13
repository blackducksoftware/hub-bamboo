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
