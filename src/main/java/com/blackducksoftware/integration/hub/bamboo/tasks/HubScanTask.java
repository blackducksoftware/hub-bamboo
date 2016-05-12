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
package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.DateTime;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bandana.BandanaManager;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.bamboo.BDBambooHubPluginException;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingPolicyStatusException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;

public class HubScanTask implements TaskType {

	private static final String ERROR_MSG_PREFIX_RESPONSE = "Response : ";
	private static final String ERROR_MSG_PREFIX_STATUS = "Status : ";
	private static final String HUB_SCAN_TASK_ERROR = "Hub Scan Task error";

	private final static String CLI_FOLDER_NAME = "tools/HubCLI";

	// private final PluginSettingsFactory pluginSettingsFactory;
	private final ProcessService processService;
	private final EnvironmentVariableAccessor environmentVariableAccessor;
	private final BandanaManager bandanaManager;

	public HubScanTask(/* final PluginSettingsFactory pluginSettingsFactory, */ final ProcessService processService,
			final EnvironmentVariableAccessor environmentVariableAccessor, final BandanaManager bandanaManager) {
		// this.pluginSettingsFactory = pluginSettingsFactory;
		this.processService = processService;
		this.environmentVariableAccessor = environmentVariableAccessor;
		this.bandanaManager = bandanaManager;
	}

	public TaskResult execute(final TaskContext taskContext) throws TaskException {

		final TaskResultBuilder resultBuilder = TaskResultBuilder.newBuilder(taskContext).success();
		final HubBambooLogger logger = new HubBambooLogger(taskContext.getBuildLogger());

		final Map<String, String> envVars = HubBambooUtils.getInstance().getEnvironmentVariablesMap(
				environmentVariableAccessor.getEnvironment(), environmentVariableAccessor.getEnvironment(taskContext));

		logger.setLogLevel(envVars);
		try {

			final ConfigurationMap taskConfigMap = taskContext.getConfigurationMap();
			final HubServerConfig hubConfig = getHubServerConfig();
			final HubIntRestService service = getService(hubConfig);
			final HubScanJobConfig jobConfig = getJobConfig(taskContext.getConfigurationMap(),
					taskContext.getWorkingDirectory(), logger);
			final HubProxyInfo proxyInfo = hubConfig.getProxyInfo();
			printGlobalConfiguration(hubConfig, proxyInfo, logger);

			if (jobConfig == null) {
				// invalid job configuration fail the build.
				logger.error("Task Configuration invalid.  Please validate configuration settings.");
				return resultBuilder.failedWithError().build();
			}
			printConfiguration(taskContext, hubConfig, logger, jobConfig);

			service.setCookies(hubConfig.getGlobalCredentials().getUsername(),
					hubConfig.getGlobalCredentials().getDecryptedPassword());

			final String localHostName = HostnameHelper.getMyHostname();
			logger.info("Running on machine : " + localHostName);

			// install the CLI
			final CLIInstaller installer = installCLI(logger, service, localHostName);

			if (installer == null || !installer.getCLIExists(logger)) {
				logger.error("Could not find the Hub scan CLI");
				resultBuilder.failed();
				return resultBuilder.build();
			}

			final File hubCLI = installer.getCLI();

			final File oneJarFile = installer.getOneJarFile();

			final File javaExec = installer.getProvidedJavaExec();

			final HubSupportHelper hubSupport = new HubSupportHelper();
			hubSupport.checkHubSupport(service, logger);

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
					javaExec, hubConfig, jobConfig, proxyInfo, hubSupport, envVars);
			final DateTime afterScanTime = new DateTime();
			// check the policy failures

			final boolean isFailOnPolicySelected = taskConfigMap
					.getAsBoolean(HubScanParamEnum.FAIL_ON_POLICY_VIOLATION.getKey());
			if (isFailOnPolicySelected && !hubSupport.isPolicyApiSupport()) {
				logger.error("This version of the Hub does not have support for Policies.");
				resultBuilder.failed();
				return resultBuilder.build();
			} else if (isFailOnPolicySelected) {

				final HubReportGenerationInfo bomUpdateInfo = new HubReportGenerationInfo();
				bomUpdateInfo.setService(service);
				bomUpdateInfo.setHostname(HostnameHelper.getMyHostname());
				bomUpdateInfo.setScanTargets(jobConfig.getScanTargetPaths());

				bomUpdateInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds());

				bomUpdateInfo.setBeforeScanTime(beforeScanTime);
				bomUpdateInfo.setAfterScanTime(afterScanTime);

				bomUpdateInfo.setScanStatusDirectory(scan.getScanStatusDirectoryPath());

				final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, service,
						hubSupport, bomUpdateInfo, version.getLink(ReleaseItem.POLICY_STATUS_LINK));

				return policyResult.build();
			}

		} catch (final HubIntegrationException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final URISyntaxException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final BDRestException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final IOException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final InterruptedException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final BDBambooHubPluginException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final IllegalArgumentException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		} catch (final EncryptionException e) {
			logger.error(HUB_SCAN_TASK_ERROR, e);
			resultBuilder.failedWithError().build();
		}

		return resultBuilder.build();
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

		return hubScanJobConfigBuilder.build().getConstructedObject();
	}

	private CLIInstaller installCLI(final IntLogger logger, final HubIntRestService restService,
			final String localHostName) {

		logger.info("Checking Hub CLI installation");
		try {
			final File toolsDir = new File(SystemProperty.BAMBOO_HOME_FROM_ENV.getValue(), CLI_FOLDER_NAME);

			// make the directories for the hub scan CLI tool
			if (!toolsDir.exists()) {
				toolsDir.mkdirs();
			}

			final CLIInstaller installer = new CLIInstaller(toolsDir);
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

	private HubIntRestService getService(final HubServerConfig hubConfig) throws MalformedURLException {
		// configure the Restlet engine so that the HTTPHandle and classes
		// from the com.sun.net.httpserver package
		// do not need to be used at runtime to make client calls.
		// DO NOT REMOVE THIS or the OSGI bundle will throw a
		// ClassNotFoundException for com.sun.net.httpserver.HttpHandler.
		// Since we are acting as a client we do not need the httpserver
		// components.

		// This workaround found here:
		// http://stackoverflow.com/questions/25179243/com-sun-net-httpserver-httphandler-classnotfound-exception-on-java-embedded-runt

		Engine.register(false);
		Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
		final HubIntRestService service = new HubIntRestService(hubConfig.getHubUrl().toString());
		HubBambooUtils.getInstance().configureProxyToService(hubConfig, service);
		return service;
	}

	public void printGlobalConfiguration(final HubServerConfig hubConfig, final HubProxyInfo proxyInfo,
			final IntLogger logger) {
		if (hubConfig == null) {
			return;
		}

		logger.info("--> Hub Server Url : " + hubConfig.getHubUrl());
		if (StringUtils.isNotBlank(hubConfig.getGlobalCredentials().getUsername())) {
			logger.info("--> Hub User : " + hubConfig.getGlobalCredentials().getUsername());
		}

		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost())) {
				logger.info("--> Proxy Host : " + proxyInfo.getHost());
			}
			if (proxyInfo.getPort() > 0) {
				logger.info("--> Proxy Port : " + proxyInfo.getPort());
			}
			if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
				logger.info("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
			}
			if (StringUtils.isNotBlank(proxyInfo.getUsername())) {
				logger.info("--> Proxy Username : " + proxyInfo.getUsername());
			}
		}
	}

	public void printConfiguration(final TaskContext taskContext, final HubServerConfig hubConfig,
			final IntLogger logger, final HubScanJobConfig jobConfig) throws IOException, InterruptedException {
		logger.info("Initializing - Hub Bamboo Plugin");

		logger.info("-> Bamboo home directory: " + SystemProperty.BAMBOO_HOME_FROM_ENV.getValue());
		final BuildContext buildContext = taskContext.getBuildContext();
		logger.info("-> Using Url : " + hubConfig.getHubUrl());
		logger.info("-> Using Username : " + hubConfig.getGlobalCredentials().getUsername());
		logger.info("-> Using Build Full Name : " + buildContext.getDisplayName());
		logger.info("-> Using Build Number : " + buildContext.getBuildNumber());
		logger.info("-> Using Build Workspace Path : " + taskContext.getWorkingDirectory().getAbsolutePath());
		logger.info(
				"-> Using Hub Project Name : " + jobConfig.getProjectName() + ", Version : " + jobConfig.getVersion()
						+ ", Phase : " + jobConfig.getPhase() + ", Distribution : " + jobConfig.getDistribution());

		logger.info("-> Scanning the following targets  : ");
		for (final String target : jobConfig.getScanTargetPaths()) {
			logger.info("-> " + target);
		}
		// logger.info("-> Generate Hub report : " +
		// jobConfig.isShouldGenerateRiskReport());
		final String formattedTime = String.format("%d minutes",
				TimeUnit.MILLISECONDS.toMinutes(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds()));
		logger.info("-> Maximum wait time for the BOM Update : " + formattedTime);
	}

	private ScanExecutor performScan(final TaskContext taskContext, final TaskResultBuilder resultBuilder,
			final IntLogger logger, final HubIntRestService service, final File oneJarFile, final File scanExec,
			File javaExec, final HubServerConfig hubConfig, final HubScanJobConfig jobConfig,
			final HubProxyInfo proxyInfo, final HubSupportHelper supportHelper, final Map<String, String> envVars)
			throws HubIntegrationException, MalformedURLException, URISyntaxException, IllegalArgumentException,
			EncryptionException {
		final BambooScanExecutor scan = new BambooScanExecutor(hubConfig.getHubUrl().toString(),
				hubConfig.getGlobalCredentials().getUsername(), hubConfig.getGlobalCredentials().getDecryptedPassword(),
				jobConfig.getScanTargetPaths(), taskContext.getBuildContext().getBuildNumber(), supportHelper);
		scan.setLogger(logger);
		scan.setTaskContext(taskContext);
		scan.setProcessService(processService);
		scan.setEnvironmentVariableAccessor(environmentVariableAccessor);

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
			String javaHome = HubBambooUtils.getInstance().getEnvironmentVariable(envVars, "JAVA_HOME", false);
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
	 */
	private ReleaseItem ensureVersionExists(final HubIntRestService service, final IntLogger logger,
			final String projectVersion, final ProjectItem project, final HubScanJobConfig jobConfig)
			throws IOException, URISyntaxException, BDBambooHubPluginException {
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
			throws IOException, URISyntaxException, BDBambooHubPluginException {
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
			waitForBomToBeUpdated(logger, service, hubSupport, bomUpdateInfo, taskContext);

			try {
				// We use this conditional in case there are other failure
				// conditions in the future
				final PolicyStatus policyStatus = service.getPolicyStatus(policyStatusUrl);
				if (policyStatus == null) {
					logger.error("Could not find any information about the Policy status of the bom.");
					return resultBuilder.failed();
				}
				if (policyStatus.getOverallStatusEnum() == PolicyStatusEnum.IN_VIOLATION) {
					return resultBuilder.failed();
				}

				if (policyStatus.getCountInViolation() == null) {
					logger.error(createPolicyCountNotFound("In Violation"));
				} else {
					logger.info(
							createPolicyCountMessage(policyStatus.getCountInViolation().getValue(), "In Violation"));
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
					logger.info(createPolicyCountMessage(policyStatus.getCountNotInViolation().getValue(),
							"Not In Violation"));
				}
				return resultBuilder.success();
			} catch (final MissingPolicyStatusException e) {
				logger.warn(e.getMessage());
				return resultBuilder.success();
			}
		} catch (final BDBambooHubPluginException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final HubIntegrationException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final URISyntaxException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final BDRestException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final InterruptedException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
			return resultBuilder.failed();
		}
	}

	private String createPolicyCountNotFound(final String type) {
		return "Could not find the number of bom entries " + type + " of a Policy.";
	}

	private String createPolicyCountMessage(final int count, final String type) {
		return "Found " + count + " bom entries to be" + type + " of a defined Policy";
	}

	private void waitForBomToBeUpdated(final IntLogger logger, final HubIntRestService service,
			final HubSupportHelper supportHelper, final HubReportGenerationInfo bomUpdateInfo,
			final TaskContext taskContext) throws BDBambooHubPluginException, InterruptedException, BDRestException,
			HubIntegrationException, URISyntaxException, IOException {

		final HubEventPolling hubEventPolling = new HubEventPolling(service);

		if (supportHelper.isCliStatusDirOptionSupport()) {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo, logger);
		} else {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo);
		}
	}

	private HubServerConfig getHubServerConfig() throws IllegalArgumentException, EncryptionException {

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

		config = HubBambooUtils.getInstance().buildConfigFromStrings(hubUrl, hubUser, hubPass, hubPassLength,
				hubProxyUrl, hubProxyPort, hubProxyNoHost, hubProxyUser, hubProxyPass, hubProxyPassLength);

		return config;

	}

	public String getPersistedValue(final String key) {
		return (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, key);
	}
}
