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
import org.json.JSONException;
import org.restlet.resource.ResourceException;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.build.artifact.ArtifactManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.atlassian.bamboo.plan.artifact.ArtifactPublishingResult;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
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
import com.blackducksoftware.integration.builder.ValidationResultEnum;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.api.HubServicesFactory;
import com.blackducksoftware.integration.hub.api.HubVersionRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooPluginHelper;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservices.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeException;
import com.blackducksoftware.integration.phone.home.exception.PropertiesLoaderException;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class HubScanTask implements TaskType {

    private static final int RISK_REPORT_MINIMUM_FILE_COUNT = 41;

    private static final String HUB_SCAN_TASK_ERROR = "Hub Scan Task error";

    private final static String CLI_FOLDER_NAME = "tools/HubCLI";

    public static final String HUB_RISK_REPORT_FILENAME = "hub_risk_report.json";

    private final EnvironmentVariableAccessor environmentVariableAccessor;

    private final BandanaManager bandanaManager;

    private final ArtifactManager artifactManager;

    private final PluginAccessor pluginAccessor;

    public HubScanTask(final EnvironmentVariableAccessor environmentVariableAccessor, final BandanaManager bandanaManager,
            final ArtifactManager artifactManager, final PluginAccessor pluginAccessor) {
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
            final HubIntRestService hubIntRestService = new HubIntRestService(restConnection);
            final HubServicesFactory services = new HubServicesFactory(restConnection);
            final String localHostName = HostnameHelper.getMyHostname();
            logger.info("Running on machine : " + localHostName);
            final HubVersionRestService versionRestService = services.createHubVersionRestService();
            String hubVersion = versionRestService.getHubVersion();
            final File toolsDir = new File(HubBambooUtils.getInstance().getBambooHome(), CLI_FOLDER_NAME);

            final HubSupportHelper hubSupport = new HubSupportHelper();
            hubSupport.checkHubSupport(versionRestService, logger);

            CLIDownloadService cliDownloadService = services.createCliDownloadService(logger);
            cliDownloadService.performInstallation(proxyInfo, toolsDir, commonEnvVars, hubConfig.getHubUrl().toString(), hubVersion, localHostName);

            // Phone-Home
            try {
                String regId = null;
                String hubHostName = null;
                try {
                    regId = hubIntRestService.getRegistrationId();
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

            // run the scan
            int scanMemory = jobConfig.getScanMemory();
            String workingDirectory = jobConfig.getWorkingDirectory();
            String projectName = jobConfig.getProjectName();
            String versionName = jobConfig.getVersion();
            boolean isDryRun = jobConfig.isDryRun();
            final SimpleScanService simpleScanService = services.createSimpleScanService(logger, restConnection, hubConfig, hubSupport, commonEnvVars, toolsDir,
                    scanMemory, true, isDryRun, projectName, versionName, jobConfig.getScanTargetPaths(), workingDirectory);

            Result scanResult = simpleScanService.setupAndExecuteScan();

            if (scanResult != Result.SUCCESS) {
                if (resultBuilder.getTaskState() != TaskState.SUCCESS) {
                    logger.error("Hub Scan Failed");
                    result = resultBuilder.build();
                    logTaskResult(logger, result);
                    return result;
                }
            } else {
                final List<ScanSummaryItem> scanSummaryList = simpleScanService.getScanSummaryItems();
                long maximumWaitTime = jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds();

                boolean waitForBom = true;
                final boolean isGenRiskReport = taskConfigMap.getAsBoolean(HubScanParamEnum.GENERATE_RISK_REPORT.getKey());

                if (isGenRiskReport && !isDryRun) {
                    generateRiskReport(taskContext, logger, scanSummaryList, services, projectName, versionName, maximumWaitTime);
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
                        final ScanStatusDataService scanStatusDataService = services.createScanStatusDataService();
                        waitForHub(scanStatusDataService, scanSummaryList, logger, maximumWaitTime);
                    }
                    final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, services, projectName, versionName,
                            isDryRun);

                    result = policyResult.build();
                }
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

    private RestConnection getRestConnection(final HubServerConfig hubConfig) throws MalformedURLException,
            URISyntaxException, IllegalArgumentException, BDRestException, EncryptionException {
        final RestConnection restConnection = new CredentialsRestConnection(hubConfig);
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
        final String formattedTime = String.format("%d minutes",
                TimeUnit.MILLISECONDS.toMinutes(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds()));
        logger.alwaysLog("-> Maximum wait time for the BOM Update : " + formattedTime);
    }

    private TaskResultBuilder checkPolicyFailures(final TaskResultBuilder resultBuilder, final TaskContext taskContext,
            final IntLogger logger, final HubServicesFactory services, String projectName, String versionName, final boolean isDryRun) {
        try {

            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return resultBuilder.success();
            }
            final PolicyStatusDataService policyStatusDataService = services.createPolicyStatusDataService();

            final PolicyStatusItem policyStatusItem = policyStatusDataService
                    .getPolicyStatusForProjectAndVersion(projectName, versionName);
            if (policyStatusItem == null) {
                logger.error("Could not find any information about the Policy status of the bom.");
                return resultBuilder.failed();
            }

            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
            final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
            if (policyStatusItem.getOverallStatus() == PolicyStatusEnum.IN_VIOLATION) {
                logger.error(policyStatusMessage);
                return resultBuilder.failedWithError();
            }
            logger.info(policyStatusMessage);
            return resultBuilder.success();
        } catch (final IOException | URISyntaxException | BDRestException | ProjectDoesNotExistException | HubIntegrationException | MissingUUIDException
                | UnexpectedHubResponseException e) {
            logger.error(e.getMessage(), e);
            return resultBuilder.failed();
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

    private void generateRiskReport(final TaskContext taskContext, final IntLogger logger, final List<ScanSummaryItem> scanSummaryList,
            final HubServicesFactory services, String projectName, String versionName, long maximumWaitTime)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException,
            UnexpectedHubResponseException, ProjectDoesNotExistException, MissingUUIDException {
        logger.info("Generating Risk Report");
        final ScanStatusDataService scanStatusDataService = services.createScanStatusDataService();
        waitForHub(scanStatusDataService, scanSummaryList, logger, maximumWaitTime);

        final Map<String, String> runtimeMap = taskContext.getRuntimeTaskContext();
        final SecureToken token = SecureToken.createFromString(runtimeMap.get(HubBambooUtils.HUB_TASK_SECURE_TOKEN));

        publishRiskReportFiles(logger, taskContext, token, services.createRiskReportDataService(logger), projectName, versionName);
    }

    private void publishRiskReportFiles(final IntLogger logger, TaskContext taskContext, SecureToken token, RiskReportDataService riskReportDataService,
            String projectName, String projectVersion) {

        final BuildContext buildContext = taskContext.getBuildContext();
        final PlanResultKey planResultKey = buildContext.getPlanResultKey();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        try {
            File baseDirectory = new File(taskContext.getWorkingDirectory() + HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME);
            riskReportDataService.createRiskReport(baseDirectory, projectName, projectVersion);
            final Map<String, String> config = new HashMap<>();
            final ArtifactDefinitionContext artifact = createArtifactDefContext(token);
            final ArtifactPublishingResult publishResult = artifactManager.publish(buildLogger, planResultKey,
                    baseDirectory, artifact, config, RISK_REPORT_MINIMUM_FILE_COUNT);

            if (!publishResult.shouldContinueBuild()) {
                logger.error("Could not publish the artifacts for the Risk Report");
            }
        } catch (IOException | URISyntaxException | BDRestException | ProjectDoesNotExistException | HubIntegrationException | InterruptedException
                | UnexpectedHubResponseException ex) {
            logger.error("Could not publish the Risk Report", ex);
        }
    }

    private ArtifactDefinitionContextImpl createArtifactDefContext(SecureToken token) {
        final ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl(
                HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME, false, token);
        artifact.setCopyPattern("**/*");
        return artifact;
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

    public void waitForHub(ScanStatusDataService scanStatusDataService, List<ScanSummaryItem> pendingScans, final IntLogger logger, final long timeout) {
        try {
            scanStatusDataService.assertBomImportScansFinished(pendingScans, timeout);
        } catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException | UnexpectedHubResponseException
                | HubIntegrationException | InterruptedException e) {
            logger.error(String.format("There was an error waiting for the scans: %s", e.getMessage()), e);
        }
    }
}
