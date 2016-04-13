package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.configuration.SystemInfo;
import com.atlassian.bamboo.process.ProcessService;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.util.concurrent.NotNull;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;
import com.blackducksoftware.integration.hub.bamboo.config.HubProxyInfo;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.util.HostnameHelper;

public class HubScanTask implements TaskType {

	private final static String CLI_FOLDER_NAME = "tools/HubCLI";

	private ConfigManager configManager;
	private SystemInfo systemInfo;
	private final ProcessService processService;

	public HubScanTask(final ProcessService processService, final SystemInfo systemInfo) {
		this.processService = processService;
		this.systemInfo = systemInfo;
	}

	public TaskResult execute(final TaskContext taskContext) throws TaskException {

		final TaskResultBuilder resultBuilder = TaskResultBuilder.newBuilder(taskContext).failed();
		final HubBambooLogger logger = new HubBambooLogger(taskContext.getBuildLogger());
		try {

			final HubConfig hubConfig = configManager.readConfig();
			final HubIntRestService service = getService(hubConfig);
			final HubScanJobConfig jobConfig = getJobConfig(taskContext.getConfigurationMap(),
					taskContext.getWorkingDirectory(), logger);
			final HubProxyInfo proxyInfo = HubBambooUtils.getInstance().createProxyInfo(hubConfig);
			printGlobalConfiguration(hubConfig, proxyInfo, logger);
			printConfiguration(taskContext, hubConfig, logger, jobConfig);

			service.setCookies(hubConfig.getHubUser(), hubConfig.getHubPass());

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

			// run the scan
			final ScanExecutor scan = performScan(taskContext, resultBuilder, logger, service, oneJarFile, hubCLI,
					javaExec, hubConfig, jobConfig, proxyInfo, hubSupport);

			// check the policy failures

			resultBuilder.success();

		} catch (final HubIntegrationException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final URISyntaxException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final BDRestException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final IOException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final InterruptedException e) {
			logger.error("Hub Scan Task error", e);
		}

		return resultBuilder.build();
	}

	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;
	}

	public void setSystemInfo(final SystemInfo systemInfo) {
		this.systemInfo = systemInfo;
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

		final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder();
		hubScanJobConfigBuilder.setProjectName(project);
		hubScanJobConfigBuilder.setVersion(version);
		hubScanJobConfigBuilder.setPhase(phase);
		hubScanJobConfigBuilder.setDistribution(distribution);
		hubScanJobConfigBuilder.setWorkingDirectory(workingDirectory.getAbsolutePath());
		hubScanJobConfigBuilder.setShouldGenerateRiskReport(generateRiskReport);
		hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTimeForBomUpdate);
		hubScanJobConfigBuilder.setScanMemory(scanMemory);
		hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
		hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();

		return hubScanJobConfigBuilder.build(logger);
	}

	private CLIInstaller installCLI(final IntLogger logger, final HubIntRestService restService,
			final String localHostName) {

		logger.info("Checking Hub CLI installation");
		try {
			final File bambooHome = new File(SystemProperty.BAMBOO_HOME_FROM_ENV.getValue());
			final File toolsDir = new File(bambooHome, CLI_FOLDER_NAME);

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
		}
		return null;
	}

	private HubIntRestService getService(final HubConfig hubConfig) {
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
		final HubIntRestService service = new HubIntRestService(hubConfig.getHubUrl());
		HubBambooUtils.getInstance().configureProxyToService(hubConfig, service);
		return service;
	}

	public void printGlobalConfiguration(final HubConfig hubConfig, final HubProxyInfo proxyInfo,
			final IntLogger logger) {
		if (hubConfig == null) {
			return;
		}

		logger.info("--> Hub Server Url : " + hubConfig.getHubUrl());
		if (StringUtils.isNotBlank(hubConfig.getHubUser())) {
			logger.info("--> Hub User : " + hubConfig.getHubUser());
		}

		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost())) {
				logger.info("--> Proxy Host : " + proxyInfo.getHost());
			}
			if (proxyInfo.getPort() != null) {
				logger.info("--> Proxy Port : " + proxyInfo.getPort());
			}
			if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
				logger.info("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
			}
			if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())) {
				logger.info("--> Proxy Username : " + proxyInfo.getProxyUsername());
			}
		}
	}

	public void printConfiguration(final TaskContext taskContext, final HubConfig hubConfig, final IntLogger logger,
			final HubScanJobConfig jobConfig) throws IOException, InterruptedException {
		logger.info("Initializing - Hub Bamboo Plugin");

		logger.info("-> Bamboo home directory: " + SystemProperty.BAMBOO_HOME_FROM_ENV.getValue());
		final BuildContext buildContext = taskContext.getBuildContext();
		logger.info("-> Using Url : " + hubConfig.getHubUrl());
		logger.info("-> Using Username : " + hubConfig.getHubUser());
		logger.info("-> Using Build Full Name : " + buildContext.getDisplayName());
		logger.info("-> Using Build Number : " + buildContext.getBuildNumber());
		logger.info("-> Using Build Workspace Path : " + taskContext.getWorkingDirectory().getAbsolutePath());
		logger.info(
				"-> Using Hub Project Name : " + jobConfig.getProjectName() + ", Version : " + jobConfig.getVersion());

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

	public ScanExecutor performScan(final TaskContext taskContext, final TaskResultBuilder resultBuilder,
			final IntLogger logger, final HubIntRestService service, final File oneJarFile, final File scanExec,
			File javaExec, final HubConfig hubConfig, final HubScanJobConfig jobConfig, final HubProxyInfo proxyInfo,
			final HubSupportHelper supportHelper)
			throws HubIntegrationException, MalformedURLException, URISyntaxException {
		final BambooScanExecutor scan = new BambooScanExecutor(hubConfig.getHubUrl(), hubConfig.getHubUser(),
				hubConfig.getHubPass(), jobConfig.getScanTargetPaths(), taskContext.getBuildContext().getBuildNumber(),
				supportHelper, processService);
		scan.setLogger(logger);

		if (proxyInfo != null) {
			final URL hubUrl = new URL(hubConfig.getHubUrl());
			if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(), proxyInfo.getNoProxyHostPatterns())) {
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
			String javaHome = getEnvironmentVariable("JAVA_HOME", logger);
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

	public void addProxySettingsToScanner(final IntLogger logger, final BambooScanExecutor scan,
			final HubProxyInfo proxyInfo) throws HubIntegrationException, URISyntaxException, MalformedURLException {
		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())
						&& StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
					scan.setProxyUsername(proxyInfo.getProxyUsername());
					scan.setProxyPassword(proxyInfo.getProxyPassword());
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

	private String getEnvironmentVariable(@NotNull final String parameterName, final IntLogger logger) {

		if (this.systemInfo == null) {
			logger.info("System info is null!!!");
		} else {
			logger.info("System info " + systemInfo);
		}
		final String result = "";
		return result;

	}

	// private boolean hasPolicyFailures(final IntLogger logger, final
	// HubSupportHelper hubSupport,
	// final HubIntRestService restService, final String projectId, final String
	// versionId) {
	//
	// // The feature is only allowed to have a single instance in the
	// // configuration therefore we just want to make
	// // sure the feature collection has something meaning that it was
	// // configured.
	//
	// if (hubSupport.isPolicyApiSupport() == false) {
	// final String message = "This version of the Hub does not have support for
	// Policies.";
	// logger.error(message);
	// return false;
	// } else {
	// try {
	// // We use this conditional in case there are other failure
	// // conditions in the future
	// final PolicyStatus policyStatus =
	// restService.getPolicyStatus(policyStatusUrl)(projectId, versionId);
	// if (policyStatus == null) {
	// final String message = "Could not find any information about the Policy
	// status of the bom.";
	// logger.error(message);
	// return false;
	// }
	// if (policyStatus.getOverallStatusEnum() == PolicyStatusEnum.IN_VIOLATION)
	// {
	// logger.error("There are Policy Violations");
	// return false;
	// }
	// if (policyStatus.getCountInViolation() == null) {
	// logger.error("Could not find the number of bom entries In Violation of a
	// Policy.");
	// } else {
	// logger.info("Found " + policyStatus.getCountInViolation().getValue()
	// + " bom entries to be In Violation of a defined Policy.");
	// }
	// if (policyStatus.getCountInViolationOverridden() == null) {
	// logger.error("Could not find the number of bom entries In Violation
	// Overridden of a Policy.");
	// } else {
	// logger.info("Found " +
	// policyStatus.getCountInViolationOverridden().getValue()
	// + " bom entries to be In Violation of a defined Policy, but they have
	// been overridden.");
	// }
	// if (policyStatus.getCountNotInViolation() == null) {
	// logger.error("Could not find the number of bom entries Not In Violation
	// of a Policy.");
	// } else {
	// logger.info("Found " + policyStatus.getCountNotInViolation().getValue()
	// + " bom entries to be Not In Violation of a defined Policy.");
	// }
	// } catch (final MissingPolicyStatusException e) {
	// logger.warn(e.getMessage());
	// } catch (final IOException e) {
	// logger.error(e.getMessage(), e);
	// return false;
	// } catch (final BDRestException e) {
	// logger.error(e.getMessage(), e);
	// return false;
	// } catch (final URISyntaxException e) {
	// logger.error(e.getMessage(), e);
	// return false;
	// }
	// }
	//
	// return true;
	// }
}
