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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.restlet.resource.ResourceException;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.plan.artifact.ArtifactPublishingResult;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskState;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.util.BuildUtils;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.utils.process.IOUtils;
import com.blackducksoftware.integration.builder.ValidationResultEnum;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.RiskReportGenerator;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.api.version.ReleaseItem;
import com.blackducksoftware.integration.hub.bamboo.BDBambooHubPluginException;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooPluginHelper;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeException;
import com.blackducksoftware.integration.phone.home.exception.PropertiesLoaderException;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubScanTask implements TaskType {

	private static final String ERROR_MSG_PREFIX_RESPONSE = "Response : ";
	private static final String ERROR_MSG_PREFIX_STATUS = "Status : ";
	private static final String HUB_SCAN_TASK_ERROR = "Hub Scan Task error";

	private final static String CLI_FOLDER_NAME = "tools/HubCLI";
	public static final String HUB_RISK_REPORT_FILENAME = "hub_risk_report.json";

	private final ProcessService processService;
	private final EnvironmentVariableAccessor environmentVariableAccessor;
	private final BandanaManager bandanaManager;
	private final ArtifactManager artifactManager;
	private final PluginAccessor pluginAccessor;

	public HubScanTask(final ProcessService processService,
			final EnvironmentVariableAccessor environmentVariableAccessor, final BandanaManager bandanaManager,
			final ArtifactManager artifactManager, final PluginAccessor pluginAccessor) {
		this.processService = processService;
		this.environmentVariableAccessor = environmentVariableAccessor;
		this.bandanaManager = bandanaManager;
		this.artifactManager = artifactManager;
		this.pluginAccessor = pluginAccessor;
	}

	@Override
	public TaskResult execute(final TaskContext taskContext) throws TaskException {

		final TaskResultBuilder resultBuilder = TaskResultBuilder.newBuilder(taskContext).success();
		TaskResult result;
		final HubBambooLogger logger = new HubBambooLogger(taskContext.getBuildLogger());

		final Map<String, String> envVars = HubBambooUtils.getInstance().getEnvironmentVariablesMap(
				environmentVariableAccessor.getEnvironment(), environmentVariableAccessor.getEnvironment(taskContext));

		final CIEnvironmentVariables commonEnvVars = new CIEnvironmentVariables();
		commonEnvVars.putAll(envVars);
		logger.setLogLevel(commonEnvVars);
		try {

			final ConfigurationMap taskConfigMap = taskContext.getConfigurationMap();
			final HubServerConfig hubConfig = getHubServerConfig(logger);

			if (hubConfig == null) {
				logger.error("Please verify the correct dependent Hub configuration plugin is installed");
				logger.error("Please verify the configuration is correct if the plugin is installed.");
				result = resultBuilder.failedWithError().build();
				logTaskResult(logger, result);
				return result;
			}
			final RestConnection restConnection = getRestConnection(hubConfig);
			final HubScanJobConfig jobConfig = getJobConfig(taskContext.getConfigurationMap(),
					taskContext.getWorkingDirectory(), logger);
			final HubProxyInfo proxyInfo = hubConfig.getProxyInfo();
			printGlobalConfiguration(hubConfig, proxyInfo, logger);

			if (jobConfig == null) {
				// invalid job configuration fail the build.
				logger.error("Task Configuration invalid.  Please validate configuration settings.");
				result = resultBuilder.failedWithError().build();
				logTaskResult(logger, result);
				return result;
			}
			final HubBambooPluginHelper pluginHelper = new HubBambooPluginHelper(pluginAccessor);

			printConfiguration(taskContext, hubConfig, logger, jobConfig, pluginHelper);
			restConnection.setCookies(hubConfig.getGlobalCredentials().getUsername(),
					hubConfig.getGlobalCredentials().getDecryptedPassword());
			final HubIntRestService service = new HubIntRestService(restConnection);
			final String localHostName = HostnameHelper.getMyHostname();
			logger.info("Running on machine : " + localHostName);

			final CLILocation cliLocation = createCLILocation(logger);

			// install the CLI
			final CLIInstaller installer = installCLI(logger, service, localHostName, cliLocation, commonEnvVars);

			if (cliLocation == null || !cliLocation.getCLIExists(logger)) {
				logger.error("Could not find the Hub scan CLI");
				resultBuilder.failed();
				result = resultBuilder.build();
				logTaskResult(logger, result);
				return result;
			}

			final File hubCLI = cliLocation.getCLI(logger);

			final File oneJarFile = cliLocation.getOneJarFile();

			final File javaExec = cliLocation.getProvidedJavaExec();

			final HubSupportHelper hubSupport = new HubSupportHelper();
			hubSupport.checkHubSupport(service, logger);

			// Phone-Home
			try {
				final String hubVersion = hubSupport.getHubVersion(service);
				String regId = null;
				String hubHostName = null;
				try {
					regId = service.getRegistrationId();
				} catch (final Exception e) {
					logger.debug("Could not get the Hub registration Id.");
				}
				try {
					final URL url = hubConfig.getHubUrl();
					hubHostName = url.getHost();
				} catch (final Exception e) {
					logger.debug("Could not get the Hub Host name.");
				}
				bdPhoneHome(logger, hubVersion, regId, hubHostName, pluginHelper);
			} catch (final Exception e) {
				logger.debug("Unable to phone-home", e);
			}

			ProjectItem project = null;
			ReleaseItem version = null;
			final String projectName = jobConfig.getProjectName();
			final String projectVersion = jobConfig.getVersion();
			if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(projectVersion)) {
				project = ensureProjectExists(service, logger, projectName);
				version = ensureVersionExists(service, logger, projectVersion, project, jobConfig);
				logger.debug("Found Project : " + projectName);
				logger.debug("Found Version : " + projectVersion);
			}

			// run the scan
			final DateTime beforeScanTime = new DateTime();
			final ScanExecutor scan = performScan(taskContext, resultBuilder, logger, service, oneJarFile, hubCLI,
					javaExec, hubConfig, jobConfig, proxyInfo, hubSupport, commonEnvVars);
			final DateTime afterScanTime = new DateTime();

			if (resultBuilder.getTaskState() != TaskState.SUCCESS) {
				logger.error("Hub Scan Failed");
				result = resultBuilder.build();
				logTaskResult(logger, result);
				return result;
			}

			// check the policy failures

			final HubReportGenerationInfo bomUpdateInfo = new HubReportGenerationInfo();
			bomUpdateInfo.setService(service);
			bomUpdateInfo.setProject(project);
			bomUpdateInfo.setVersion(version);
			bomUpdateInfo.setHostname(HostnameHelper.getMyHostname());
			bomUpdateInfo.setScanTargets(jobConfig.getScanTargetPaths());
			bomUpdateInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds());

			bomUpdateInfo.setBeforeScanTime(beforeScanTime);
			bomUpdateInfo.setAfterScanTime(afterScanTime);

			bomUpdateInfo.setScanStatusDirectory(scan.getScanStatusDirectoryPath());

			boolean waitForBom = true;
			final boolean isGenRiskReport = taskConfigMap.getAsBoolean(HubScanParamEnum.GENERATE_RISK_REPORT.getKey());

			if (isGenRiskReport) {
				generateRiskReport(taskContext, logger, bomUpdateInfo, hubSupport);
				waitForBom = false;
			}

			final boolean isFailOnPolicySelected = taskConfigMap
					.getAsBoolean(HubScanParamEnum.FAIL_ON_POLICY_VIOLATION.getKey());
			if (isFailOnPolicySelected && !hubSupport.hasCapability(HubCapabilitiesEnum.POLICY_API)) {
				logger.error("This version of the Hub does not have support for Policies.");
				resultBuilder.failed();
				result = resultBuilder.build();
				logTaskResult(logger, result);
				return result;
			} else if (isFailOnPolicySelected) {

				if (waitForBom) {
					waitForBomToBeUpdated(logger, service, hubSupport, bomUpdateInfo, taskContext);
				}
				final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, service,
						hubSupport, bomUpdateInfo, version.getLink(ReleaseItem.POLICY_STATUS_LINK));

				result = policyResult.build();
			}

		} catch (final Exception e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			result = resultBuilder.failedWithError().build();
		}

		result = resultBuilder.build();
		logTaskResult(logger, result);
		return result;
	}

	private void logTaskResult(final IntLogger logger, final TaskResult result) {
		logger.info("HUB Scan Task result: " + result.getTaskState());
	}

	private HubScanJobConfig getJobConfig(final ConfigurationMap configMap, final File workingDirectory,
			final IntLogger logger) throws HubIntegrationException, IOException {
		final String project = configMap.get(HubScanParamEnum.PROJECT.getKey());
		final String version = configMap.get(HubScanParamEnum.VERSION.getKey());
		final String phase = configMap.get(HubScanParamEnum.PHASE.getKey());
		final String distribution = configMap.get(HubScanParamEnum.DISTRIBUTION.getKey());
		final String generateRiskReport = configMap.get(HubScanParamEnum.GENERATE_RISK_REPORT.getKey());
		final String maxWaitTimeForBomUpdate = configMap.get(HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey());
		final String scanMemory = configMap.get(HubScanParamEnum.SCANMEMORY.getKey());
		final String targets = configMap.get(HubScanParamEnum.TARGETS.getKey());

		final List<String> scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(targets, workingDirectory);

		if (scanTargets.isEmpty()) {
			// no targets specified assume the working directory.
			scanTargets.add(workingDirectory.getAbsolutePath());
		}

		final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder(false);
		hubScanJobConfigBuilder.setProjectName(project);
		hubScanJobConfigBuilder.setVersion(version);
		hubScanJobConfigBuilder.setPhase(PhaseEnum.getPhaseByDisplayValue(phase).name());
		hubScanJobConfigBuilder.setDistribution(DistributionEnum.getDistributionByDisplayValue(distribution).name());
		hubScanJobConfigBuilder.setWorkingDirectory(workingDirectory.getAbsolutePath());
		hubScanJobConfigBuilder.setShouldGenerateRiskReport(generateRiskReport);
		hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTimeForBomUpdate);
		hubScanJobConfigBuilder.setScanMemory(scanMemory);
		hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
		hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();

		return hubScanJobConfigBuilder.build();
	}

	private CLILocation createCLILocation(final IntLogger logger) {
		logger.info("Checking Hub CLI location.");
		final File toolsDir = new File(HubBambooUtils.getInstance().getBambooHome(), CLI_FOLDER_NAME);

		// make the directories for the hub scan CLI tool
		if (!toolsDir.exists()) {
			toolsDir.mkdirs();
		}
		return new CLILocation(toolsDir);
	}

	private CLIInstaller installCLI(final IntLogger logger, final HubIntRestService restService,
			final String localHostName, final CLILocation cliLocation,
			final CIEnvironmentVariables ciEnvironmentVariables) {

		logger.info("Checking Hub CLI installation");
		try {
			final CLIInstaller installer = new CLIInstaller(cliLocation, ciEnvironmentVariables);
			installer.performInstallation(logger, restService, localHostName);
			return installer;
		} catch (final IOException e) {
			logger.error(e);
		} catch (final InterruptedException e) {
			logger.error(e);
		} catch (final BDRestException e) {
			logger.error(e);
		} catch (final URISyntaxException e) {
			logger.error(e);
		} catch (final HubIntegrationException e) {
			logger.error(e);
		} catch (final Exception e) {
			logger.error(e);
		}

		return null;
	}

	private RestConnection getRestConnection(final HubServerConfig hubConfig)
			throws MalformedURLException, URISyntaxException {
		String huburl = null;
		if (hubConfig != null && hubConfig.getHubUrl() != null) {
			huburl = hubConfig.getHubUrl().toString();
		}
		final RestConnection restConnection = new RestConnection(huburl);
		HubBambooUtils.getInstance().configureProxyToService(hubConfig, restConnection);
		return restConnection;
	}

	public void printGlobalConfiguration(final HubServerConfig hubConfig, final HubProxyInfo proxyInfo,
			final HubBambooLogger logger) {
		if (hubConfig == null) {
			return;
		}

		logger.alwaysLog("--> Hub Server Url : " + hubConfig.getHubUrl());
		if (StringUtils.isNotBlank(hubConfig.getGlobalCredentials().getUsername())) {
			logger.alwaysLog("--> Hub User : " + hubConfig.getGlobalCredentials().getUsername());
		}

		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost())) {
				logger.alwaysLog("--> Proxy Host : " + proxyInfo.getHost());
			}
			if (proxyInfo.getPort() > 0) {
				logger.alwaysLog("--> Proxy Port : " + proxyInfo.getPort());
			}
			if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
				logger.alwaysLog("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
			}
			if (StringUtils.isNotBlank(proxyInfo.getUsername())) {
				logger.alwaysLog("--> Proxy Username : " + proxyInfo.getUsername());
			}
		}
	}

	public void printConfiguration(final TaskContext taskContext, final HubServerConfig hubConfig,
			final HubBambooLogger logger, final HubScanJobConfig jobConfig, final HubBambooPluginHelper pluginHelper)
					throws IOException, InterruptedException {
		logger.alwaysLog("Initializing - Hub Bamboo Plugin : " + pluginHelper.getPluginVersion());
		logger.alwaysLog("Log Level : " + logger.getLogLevel().name());
		logger.alwaysLog("-> Bamboo home directory: " + HubBambooUtils.getInstance().getBambooHome());
		final BuildContext buildContext = taskContext.getBuildContext();
		logger.alwaysLog("-> Using Url : " + hubConfig.getHubUrl());
		logger.alwaysLog("-> Using Username : " + hubConfig.getGlobalCredentials().getUsername());
		logger.alwaysLog("-> Using Build Full Name : " + buildContext.getDisplayName());
		logger.alwaysLog("-> Using Build Number : " + buildContext.getBuildNumber());
		logger.alwaysLog("-> Using Build Workspace Path : " + taskContext.getWorkingDirectory().getAbsolutePath());
		logger.alwaysLog(
				"-> Using Hub Project Name : " + jobConfig.getProjectName() + ", Version : " + jobConfig.getVersion()
				+ ", Phase : " + jobConfig.getPhase() + ", Distribution : " + jobConfig.getDistribution());

		logger.alwaysLog("-> Scanning the following targets  : ");
		for (final String target : jobConfig.getScanTargetPaths()) {
			logger.alwaysLog("-> " + target);
		}
		// logger.info("-> Generate Hub report : " +
		// jobConfig.isShouldGenerateRiskReport());
		final String formattedTime = String.format("%d minutes",
				TimeUnit.MILLISECONDS.toMinutes(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds()));
		logger.alwaysLog("-> Maximum wait time for the BOM Update : " + formattedTime);
	}

	private ScanExecutor performScan(final TaskContext taskContext, final TaskResultBuilder resultBuilder,
			final IntLogger logger, final HubIntRestService service, final File oneJarFile, final File scanExec,
			File javaExec, final HubServerConfig hubConfig, final HubScanJobConfig jobConfig,
			final HubProxyInfo proxyInfo, final HubSupportHelper supportHelper,
			final CIEnvironmentVariables commonEnvVars)
					throws HubIntegrationException, MalformedURLException, URISyntaxException, IllegalArgumentException,
					EncryptionException {
		final BambooScanExecutor scan = new BambooScanExecutor(hubConfig.getHubUrl().toString(),
				hubConfig.getGlobalCredentials().getUsername(), hubConfig.getGlobalCredentials().getDecryptedPassword(),
				jobConfig.getScanTargetPaths(), String.valueOf(taskContext.getBuildContext().getBuildNumber()),
				supportHelper);
		scan.setLogger(logger);
		scan.setTaskContext(taskContext);
		scan.setProcessService(processService);
		scan.setCommonEnvVars(commonEnvVars);

		if (proxyInfo != null) {
			final URL hubUrl = hubConfig.getHubUrl();
			if (!proxyInfo.shouldUseProxyForUrl(hubUrl)) {
				addProxySettingsToScanner(logger, scan, proxyInfo);
			}
		}

		scan.setScanMemory(jobConfig.getScanMemory());
		scan.setWorkingDirectory(jobConfig.getWorkingDirectory());
		scan.setVerboseRun(true);
		if (StringUtils.isNotBlank(jobConfig.getProjectName()) && StringUtils.isNotBlank(jobConfig.getVersion())) {

			scan.setProject(jobConfig.getProjectName());
			scan.setVersion(jobConfig.getVersion());
		}

		if (javaExec == null) {
			String javaHome = commonEnvVars.getValue("JAVA_HOME");
			if (StringUtils.isBlank(javaHome)) {
				// We couldn't get the JAVA_HOME variable so lets try to get the
				// home
				// of the java that is running this process
				javaHome = System.getProperty("java.home");
			}
			javaExec = new File(javaHome);
			if (StringUtils.isBlank(javaHome) || javaExec == null || !javaExec.exists()) {
				throw new HubIntegrationException(
						"The JAVA_HOME could not be determined, the Hub CLI can not be executed.");
			}
			javaExec = new File(javaExec, "bin");
			if (SystemUtils.IS_OS_WINDOWS) {
				javaExec = new File(javaExec, "java.exe");
			} else {
				javaExec = new File(javaExec, "java");
			}
		}

		final Result scanResult = scan.setupAndRunScan(scanExec.getAbsolutePath(), oneJarFile.getAbsolutePath(),
				javaExec.getAbsolutePath());
		if (scanResult != Result.SUCCESS) {
			resultBuilder.failed(); // fail build
		}

		return scan;
	}

	private void addProxySettingsToScanner(final IntLogger logger, final BambooScanExecutor scan,
			final HubProxyInfo proxyInfo) throws HubIntegrationException, URISyntaxException, MalformedURLException,
	IllegalArgumentException, EncryptionException {
		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getUsername())
						&& StringUtils.isNotBlank(proxyInfo.getDecryptedPassword())) {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
					scan.setProxyUsername(proxyInfo.getUsername());
					scan.setProxyPassword(proxyInfo.getDecryptedPassword());
				} else {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
				}
				if (logger != null) {
					logger.debug("Using proxy: '" + proxyInfo.getHost() + "' at Port: '" + proxyInfo.getPort() + "'");
				}
			}
		}
	}

	private ProjectItem ensureProjectExists(final HubIntRestService service, final IntLogger logger,
			final String projectName) throws IOException, URISyntaxException, BDBambooHubPluginException {
		ProjectItem project = null;
		try {
			project = service.getProjectByName(projectName);

		} catch (final NullPointerException npe) {
			project = createProject(service, logger, projectName);
		} catch (final ProjectDoesNotExistException e) {
			project = createProject(service, logger, projectName);
		} catch (final BDRestException e) {
			if (e.getResource() != null) {
				if (e.getResource() != null) {
					logger.error(ERROR_MSG_PREFIX_STATUS + e.getResource().getStatus().getCode());
					logger.error(ERROR_MSG_PREFIX_RESPONSE + e.getResource().getResponse().getEntityAsText());
				}
				throw new BDBambooHubPluginException("Problem getting the Project. ", e);
			}
		}

		return project;
	}

	private ProjectItem createProject(final HubIntRestService service, final IntLogger logger, final String projectName)
			throws IOException, URISyntaxException, BDBambooHubPluginException {
		// Project was not found, try to create it
		ProjectItem project = null;
		try {
			final String projectUrl = service.createHubProject(projectName);
			project = service.getProject(projectUrl);
		} catch (final BDRestException e1) {
			if (e1.getResource() != null) {
				logger.error(ERROR_MSG_PREFIX_STATUS + e1.getResource().getStatus().getCode());
				logger.error(ERROR_MSG_PREFIX_RESPONSE + e1.getResource().getResponse().getEntityAsText());
			}
			throw new BDBambooHubPluginException("Problem creating the Project. ", e1);
		}

		return project;
	}

	/**
	 * Ensures the Version exists. Returns the version URL
	 *
	 * @throws UnexpectedHubResponseException
	 */
	private ReleaseItem ensureVersionExists(final HubIntRestService service, final IntLogger logger,
			final String projectVersion, final ProjectItem project, final HubScanJobConfig jobConfig)
					throws IOException, URISyntaxException, BDBambooHubPluginException, UnexpectedHubResponseException {
		ReleaseItem version = null;

		try {
			version = service.getVersion(project, projectVersion);
			if (!version.getPhase().equals(jobConfig.getPhase())) {
				logger.warn(
						"The selected Phase does not match the Phase of this Version. If you wish to update the Phase please do so in the Hub UI.");
			}
			if (!version.getDistribution().equals(jobConfig.getDistribution())) {
				logger.warn(
						"The selected Distribution does not match the Distribution of this Version. If you wish to update the Distribution please do so in the Hub UI.");
			}
		} catch (final NullPointerException npe) {
			version = createVersion(service, logger, projectVersion, project, jobConfig);
		} catch (final VersionDoesNotExistException e) {
			version = createVersion(service, logger, projectVersion, project, jobConfig);
		} catch (final BDRestException e) {
			throw new BDBambooHubPluginException("Could not retrieve or create the specified version.", e);
		}
		return version;
	}

	private ReleaseItem createVersion(final HubIntRestService service, final IntLogger logger,
			final String projectVersion, final ProjectItem project, final HubScanJobConfig jobConfig)
					throws IOException, URISyntaxException, BDBambooHubPluginException, UnexpectedHubResponseException {
		ReleaseItem version = null;

		try {
			final String versionURL = service.createHubVersion(project, projectVersion, jobConfig.getPhase(),
					jobConfig.getDistribution());
			version = service.getProjectVersion(versionURL);
		} catch (final BDRestException e1) {
			if (e1.getResource() != null) {
				logger.error(ERROR_MSG_PREFIX_STATUS + e1.getResource().getStatus().getCode());
				logger.error(ERROR_MSG_PREFIX_RESPONSE + e1.getResource().getResponse().getEntityAsText());
			}
			throw new BDBambooHubPluginException("Problem creating the Version. ", e1);
		}

		return version;
	}

	private TaskResultBuilder checkPolicyFailures(final TaskResultBuilder resultBuilder, final TaskContext taskContext,
			final IntLogger logger, final HubIntRestService service, final HubSupportHelper hubSupport,
			final HubReportGenerationInfo bomUpdateInfo, final String policyStatusUrl) {
		try {
			// We use this conditional in case there are other failure
			// conditions in the future
			final PolicyStatusItem policyStatus = service.getPolicyStatus(policyStatusUrl);
			if (policyStatus == null) {
				logger.error("Could not find any information about the Policy status of the bom.");
				return resultBuilder.failed();
			}

			if (policyStatus.getCountInViolation() == null) {
				logger.error(createPolicyCountNotFound("In Violation"));
			} else {
				final String inViolationMsg = createPolicyCountMessage(policyStatus.getCountInViolation().getValue(),
						"In Violation");
				if (policyStatus.getOverallStatus() == PolicyStatusEnum.IN_VIOLATION) {
					logger.error(inViolationMsg);
				} else {
					logger.info(inViolationMsg);
				}
			}
			if (policyStatus.getCountInViolationOverridden() == null) {
				logger.error(createPolicyCountNotFound("In Violation Overridden"));
			} else {
				logger.info(createPolicyCountMessage(policyStatus.getCountInViolationOverridden().getValue(),
						"In Violation") + ", but they have been overridden.");
			}
			if (policyStatus.getCountNotInViolation() == null) {
				logger.error(createPolicyCountNotFound("Not In Violation"));
			} else {
				logger.info(
						createPolicyCountMessage(policyStatus.getCountNotInViolation().getValue(), "Not In Violation"));
			}

			if (policyStatus.getOverallStatus() == PolicyStatusEnum.IN_VIOLATION) {
				return resultBuilder.failedWithError();
			}
			return resultBuilder.success();
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final BDRestException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final URISyntaxException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		}
	}

	private String createPolicyCountNotFound(final String type) {
		return "Could not find the number of bom entries " + type + " of a Policy.";
	}

	private String createPolicyCountMessage(final int count, final String type) {
		return "Found " + count + " bom entries to be " + type + " of a defined Policy";
	}

	private void waitForBomToBeUpdated(final IntLogger logger, final HubIntRestService service,
			final HubSupportHelper supportHelper, final HubReportGenerationInfo bomUpdateInfo,
			final TaskContext taskContext) throws BDBambooHubPluginException, InterruptedException, BDRestException,
	HubIntegrationException, URISyntaxException, IOException, ProjectDoesNotExistException,
	MissingUUIDException, UnexpectedHubResponseException {

		final HubEventPolling hubEventPolling = new HubEventPolling(service);

		if (supportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION)) {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo, logger);
		} else {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo);
		}
	}

	private HubServerConfig getHubServerConfig(final IntLogger logger)
			throws IllegalArgumentException, EncryptionException {

		HubServerConfig config = null;

		final String hubUrl = getPersistedValue(HubConfigKeys.CONFIG_HUB_URL);
		final String hubUser = getPersistedValue(HubConfigKeys.CONFIG_HUB_USER);
		final String hubPass = getPersistedValue(HubConfigKeys.CONFIG_HUB_PASS);
		final String hubPassLength = getPersistedValue(HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
		final String hubProxyUrl = getPersistedValue(HubConfigKeys.CONFIG_PROXY_HOST);
		final String hubProxyPort = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PORT);
		final String hubProxyNoHost = getPersistedValue(HubConfigKeys.CONFIG_PROXY_NO_HOST);
		final String hubProxyUser = getPersistedValue(HubConfigKeys.CONFIG_PROXY_USER);
		final String hubProxyPass = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PASS);
		final String hubProxyPassLength = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

		final ValidationResults<GlobalFieldKey, HubServerConfig> results = HubBambooUtils.getInstance()
				.buildConfigFromStrings(hubUrl, hubUser, hubPass, hubPassLength, hubProxyUrl, hubProxyPort,
						hubProxyNoHost, hubProxyUser, hubProxyPass, hubProxyPassLength);

		if (results.isSuccess()) {
			config = results.getConstructedObject();
		} else {
			logger.error("Hub Server Configuration Invalid.");
			final Set<GlobalFieldKey> keySet = results.getResultMap().keySet();
			for (final GlobalFieldKey key : keySet) {
				if (results.hasErrors(key)) {
					logger.error(results.getResultString(key, ValidationResultEnum.ERROR));
				}

				if (results.hasWarnings(key)) {
					logger.warn(results.getResultString(key, ValidationResultEnum.ERROR));
				}
			}

		}
		return config;

	}

	private String getPersistedValue(final String key) {
		return (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, key);
	}

	private void generateRiskReport(final TaskContext taskContext, final IntLogger logger,
			final HubReportGenerationInfo hubReportGenerationInfo, final HubSupportHelper hubSupport)
					throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException,
					UnexpectedHubResponseException, ProjectDoesNotExistException, MissingUUIDException {
		logger.info("Generating Risk Report");

		final RiskReportGenerator riskReportGenerator = new RiskReportGenerator(hubReportGenerationInfo, hubSupport);
		// will wait for bom to be updated while generating the
		// report.
		final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
		final HubRiskReportData hubRiskReportData = riskReportGenerator.generateHubReport(logger, categories);
		final String reportPath = taskContext.getWorkingDirectory() + File.separator
				+ HubBambooUtils.HUB_RISK_REPORT_FILENAME;

		final Gson gson = new GsonBuilder().create();
		final String contents = gson.toJson(hubRiskReportData);

		FileWriter writer = null;
		try {
			writer = new FileWriter(reportPath);
			writer.write(contents);
		} finally {
			IOUtils.closeQuietly(writer);
		}

		final BuildContext buildContext = taskContext.getBuildContext();
		final PlanResultKey planResultKey = buildContext.getPlanResultKey();
		final BuildLogger buildLogger = taskContext.getBuildLogger();
		final File baseDirectory = taskContext.getWorkingDirectory();

		final Map<String, String> runtimeMap = taskContext.getRuntimeTaskContext();

		final SecureToken token = SecureToken.createFromString(runtimeMap.get(HubBambooUtils.HUB_TASK_SECURE_TOKEN));
		final ArtifactDefinitionContext artifact = HubBambooUtils.getInstance()
				.getRiskReportArtifactDefinitionContext(token);

		final Map<String, String> config = new HashMap<>();

		final ArtifactPublishingResult publishResult = artifactManager.publish(buildLogger, planResultKey,
				baseDirectory, artifact, config, 1);

		if (!publishResult.shouldContinueBuild()) {
			logger.error("Could not publish the " + HubBambooUtils.HUB_RISK_REPORT_FILENAME
					+ " artifact for the Risk Report");
		}
	}

	/**
	 * @param blackDuckVersion
	 *            Version of the blackduck product, in this instance, the hub
	 * @param regId
	 *            Registration ID of the hub instance that this plugin uses
	 * @param hubHostName
	 *            Host name of the hub instance that this plugin uses
	 *
	 *            This method "phones-home" to the internal BlackDuck
	 *            Integrations server.
	 */
	public void bdPhoneHome(final HubBambooLogger logger, final String blackDuckVersion, final String regId,
			final String hubHostName, final HubBambooPluginHelper pluginHelper)
					throws IOException, PhoneHomeException, PropertiesLoaderException, ResourceException, JSONException {

		final String thirdPartyVersion = BuildUtils.getCurrentVersion();
		final String pluginVersion = pluginHelper.getPluginVersion();

		final PhoneHomeClient phClient = new PhoneHomeClient();
		phClient.callHomeIntegrations(regId, hubHostName, BlackDuckName.HUB, blackDuckVersion, ThirdPartyName.BAMBOO,
				thirdPartyVersion, pluginVersion);
	}

}
