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
package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScannerSplitStream;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class BambooScanExecutor extends ScanExecutor {

	private static final String CLI_PARAM_PASSWORD = "--password";
	private static final String CLI_PARAM_LOG_DIR = "--logDir";
	private ProcessService processService;
	private TaskContext taskContext;
	private CIEnvironmentVariables commonEnvVars;

	protected BambooScanExecutor(final String hubUrl, final String hubUsername, final String hubPassword,
			final List<String> scanTargets, final String buildIdentifier, final HubSupportHelper supportHelper) {
		super(hubUrl, hubUsername, hubPassword, scanTargets, buildIdentifier, supportHelper);
	}

	public ProcessService getProcessService() {
		return processService;
	}

	public void setProcessService(final ProcessService processService) {
		this.processService = processService;
	}

	public TaskContext getTaskContext() {
		return taskContext;
	}

	public void setTaskContext(final TaskContext taskContext) {
		this.taskContext = taskContext;
	}

	public CIEnvironmentVariables getCommonEnvVars() {
		return commonEnvVars;
	}

	public void setCommonEnvVars(final CIEnvironmentVariables commonEnvVars) {
		this.commonEnvVars = commonEnvVars;
	}

	@Override
	protected String getLogDirectoryPath() throws IOException {
		File logDirectory = new File(getWorkingDirectory());
		logDirectory = new File(logDirectory, "HubScanLogs");
		logDirectory = new File(logDirectory, String.valueOf(getBuildIdentifier()));
		logDirectory.mkdirs();

		return logDirectory.getAbsolutePath();
	}

	@Override
	protected Result executeScan(final List<String> cmd, final String logDirectoryPath)
			throws HubIntegrationException, InterruptedException {
		try {
			final File logBaseDirectory = new File(getLogDirectoryPath());
			logBaseDirectory.mkdirs();
			final File standardOutFile = new File(logBaseDirectory, "CLI_Output.txt");
			standardOutFile.createNewFile();

			printCommand(cmd);

			// Should use the split stream for the process

			final FileOutputStream outputFileStream = new FileOutputStream(standardOutFile);

			String outputString = "";
			int returnCode;
			try (ScannerSplitStream splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream)) {

				// we should use Bamboo's ExternalProcessBuilder and
				// ExternalProcess
				// but it logs all the output from the scan CLI to the screen
				// which
				// is not what we want to do So use Java's default process
				// builder
				// since a task is invoking this and tasks run on agents.
				final ProcessBuilder procBuilder = createProcessBuilder(cmd, getCommonEnvVars());
				final Process hubCliProcess = procBuilder.start();

				// The Cli logs go the error stream for some reason
				final StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(),
						splitOutputStream);
				redirectThread.start();

				// Would like to encapsulate the common code however the
				// waitFor() blocks indefinitely if the the process is started
				// in another method.
				returnCode = hubCliProcess.waitFor();

				// the join method on the redirect thread will wait until the
				// thread is dead. The thread will die when it reaches the end
				// of stream and the run method is finished
				redirectThread.join();

				if (splitOutputStream.hasOutput()) {
					outputString = splitOutputStream.getOutput();
				}
				getLogger().info(readStream(hubCliProcess.getInputStream()));
			}
			if (returnCode != 0 && outputString.contains("Illegal character in path")) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				try (ScannerSplitStream splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream)) {

					// This version of the CLI can not handle spaces in the log
					// directory
					// Not sure which version of the CLI this issue was fixed

					final int indexOfLogOption = cmd.indexOf(CLI_PARAM_LOG_DIR) + 1;

					String logPath = cmd.get(indexOfLogOption);
					logPath = logPath.replace(" ", "%20");
					cmd.remove(indexOfLogOption);
					cmd.add(indexOfLogOption, logPath);
					final ProcessBuilder procBuilder = createProcessBuilder(cmd, getCommonEnvVars());
					final Process hubCliProcess = procBuilder.start();
					// The Cli logs go the error stream for some reason
					final StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(),
							splitOutputStream);
					redirectThread.start();

					returnCode = hubCliProcess.waitFor();

					// the join method on the redirect thread will wait until
					// the thread is dead. The thread will die when it reaches
					// the end of stream and the run method is finished
					redirectThread.join();

					if (splitOutputStream.hasOutput()) {
						outputString = splitOutputStream.getOutput();
					}
					getLogger().info(readStream(hubCliProcess.getInputStream()));
				}
			} else if (returnCode != 0 && outputString.contains("Illegal character in opaque")) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				try (ScannerSplitStream splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream)) {

					final int indexOfLogOption = cmd.indexOf(CLI_PARAM_LOG_DIR) + 1;

					String logPath = cmd.get(indexOfLogOption);

					final File logFile = new File(logPath);

					logPath = logFile.toURI().toString();
					cmd.remove(indexOfLogOption);
					cmd.add(indexOfLogOption, logPath);

					final ProcessBuilder procBuilder = createProcessBuilder(cmd, getCommonEnvVars());
					final Process hubCliProcess = procBuilder.start();
					// The Cli logs go the error stream for some reason
					final StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(),
							splitOutputStream);
					redirectThread.start();

					returnCode = hubCliProcess.waitFor();

					// the join method on the redirect thread will wait until
					// the thread is dead. The thread will die when it reaches
					// the end of stream and the run method is finished
					redirectThread.join();

					splitOutputStream.flush();
					splitOutputStream.close();

					if (splitOutputStream.hasOutput()) {
						outputString = splitOutputStream.getOutput();
					}
					getLogger().info(readStream(hubCliProcess.getInputStream()));
				}
			}

			getLogger().info("Hub CLI return code : " + returnCode);

			if (logDirectoryPath != null) {
				final File logDirectory = new File(logDirectoryPath);
				if (logDirectory.exists()) {
					getLogger().info(
							"You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getAbsolutePath() + "'");
					getLogger().info("");
				}
			}

			if (returnCode == 0) {
				return Result.SUCCESS;
			} else {
				return Result.FAILURE;
			}
		} catch (final MalformedURLException e) {
			throw new HubIntegrationException("The server URL provided was not a valid", e);
		} catch (final IOException e) {
			throw new HubIntegrationException(e.getMessage(), e);
		} catch (final InterruptedException e) {
			throw new HubIntegrationException(e.getMessage(), e);
		}
	}

	private ProcessBuilder createProcessBuilder(final List<String> cmd, final CIEnvironmentVariables commonEnvVars) {
		final ProcessBuilder builder = new ProcessBuilder(cmd).redirectError(Redirect.PIPE)
				.redirectOutput(Redirect.PIPE);
		builder.environment().put("BD_HUB_PASSWORD", getHubPassword());

		if (commonEnvVars != null) {
			final String bdioEnvVar = commonEnvVars.getValue("BD_HUB_DECLARED_COMPONENTS");
			if (StringUtils.isNotBlank(bdioEnvVar)) {
				builder.environment().put("BD_HUB_DECLARED_COMPONENTS", bdioEnvVar);
			}
		}
		return builder;
	}

	private void printCommand(final List<String> cmd) {
		// ////////////////////// Code to mask the password in the logs
		final List<String> cmdToOutput = new ArrayList<>();
		cmdToOutput.addAll(cmd);

		final ArrayList<Integer> indexToMask = new ArrayList<>();
		final int passParamIndex = cmdToOutput.indexOf(CLI_PARAM_PASSWORD);

		if (passParamIndex > -1) {
			// The User's password will be at the next index
			indexToMask.add(passParamIndex + 1);
		}

		for (int i = 0; i < cmdToOutput.size(); i++) {
			if (cmdToOutput.get(i).contains("-Dhttp") && cmdToOutput.get(i).contains("proxyPassword")) {
				indexToMask.add(i);
			}
		}
		for (final Integer index : indexToMask) {
			maskIndex(cmdToOutput, index);
		}

		// ///////////////////////
		getLogger().info("Hub CLI command :");
		for (final String current : cmdToOutput) {
			getLogger().info(current);
		}
	}

	private void maskIndex(final List<String> cmd, final int indexToMask) {
		final String cmdToMask = cmd.get(indexToMask);
		final String[] maskedArray = new String[cmdToMask.length()];
		Arrays.fill(maskedArray, "*");
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String current : maskedArray) {
			stringBuilder.append(current);
		}
		final String maskedCmd = stringBuilder.toString();

		cmd.remove(indexToMask);
		cmd.add(indexToMask, maskedCmd);
	}

	private String readStream(final InputStream stream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		final StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + System.lineSeparator());
		}
		return stringBuilder.toString();
	}
}
