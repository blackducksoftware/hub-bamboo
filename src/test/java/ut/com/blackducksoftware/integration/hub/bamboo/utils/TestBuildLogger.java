/**
 * Black Duck Hub Plugin for Bamboo
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package ut.com.blackducksoftware.integration.hub.bamboo.utils;

import java.util.List;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.build.logger.LogInterceptorStack;
import com.atlassian.bamboo.build.logger.LogMutatorStack;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.blackducksoftware.integration.log.IntBufferedLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class TestBuildLogger implements BuildLogger {

	private final IntBufferedLogger logger = new IntBufferedLogger();

	@Override
	public String addBuildLogEntry(final LogEntry arg0) {
		return null;
	}

	@Override
	public String addBuildLogEntry(final String arg0) {
		logger.info(arg0);
		return null;
	}

	@Override
	public String addBuildLogHeader(final String arg0, final boolean arg1) {
		return null;
	}

	@Override
	public String addErrorLogEntry(final LogEntry arg0) {
		return null;
	}

	@Override
	public String addErrorLogEntry(final String arg0) {
		logger.error(arg0);
		return null;
	}

	@Override
	public void addErrorLogEntry(final String arg0, final Throwable arg1) {
		logger.error(arg0, arg1);
	}

	@Override
	public void clearBuildLog() {
		logger.resetAllLogs();
	}

	@Override
	public void close() {
		logger.resetAllLogs();
	}

	@Override
	public List<LogEntry> getBuildLog() {
		return null;
	}

	@Override
	public List<LogEntry> getErrorLog() {
		return null;
	}

	@Override
	public LogInterceptorStack getInterceptorStack() {
		return null;
	}

	@Override
	public List<LogEntry> getLastNLogEntries(final int arg0) {
		return null;
	}

	public List<String> getStringBuildLogs() {
		return logger.getOutputList(LogLevel.INFO);
	}

	@Override
	public List<String> getStringErrorLogs() {
		return logger.getOutputList(LogLevel.ERROR);
	}

	@Override
	public long getTimeOfLastLog() {
		return 0;
	}

	public void startStreamingBuildLogs(final PlanResultKey arg0) {

	}

	@Override
	public void stopStreamingBuildLogs() {

	}

	@Override
	public LogMutatorStack getMutatorStack() {
		return new LogMutatorStack();
	}
}
