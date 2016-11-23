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
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.api.HubServicesFactory;
import com.blackducksoftware.integration.hub.api.HubVersionRestService;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationItem;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.report.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.api.report.ReportCategoriesEnum;
import com.blackducksoftware.integration.hub.api.report.ReportFormatEnum;
import com.blackducksoftware.integration.hub.api.report.ReportRestService;
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
            final DateTime beforeScanTime = new DateTime();
            int scanMemory = jobConfig.getScanMemory();
            String workingDirectory = jobConfig.getWorkingDirectory();
            String projectName = jobConfig.getProjectName();
            String versionName = jobConfig.getVersion();
            boolean isDryRun = jobConfig.isDryRun();
            final SimpleScanService simpleScanService = services.createSimpleScanService(logger, restConnection, hubConfig, hubSupport, commonEnvVars, toolsDir,
                    scanMemory, true, isDryRun, projectName, versionName, jobConfig.getScanTargetPaths(), workingDirectory);

            Result scanResult = simpleScanService.setupAndExecuteScan();
            final DateTime afterScanTime = new DateTime();

            if (scanResult != Result.SUCCESS) {
                if (resultBuilder.getTaskState() != TaskState.SUCCESS) {
                    logger.error("Hub Scan Failed");
                    result = resultBuilder.build();
                    logTaskResult(logger, result);
                    return result;
                }
            } else {
                final List<ScanSummaryItem> scanSummaryList = simpleScanService.getScanSummaryItems();

                ProjectVersionItem version = null;
                ProjectItem project = null;
                if (StringUtils.isNotBlank(jobConfig.getProjectName()) && StringUtils.isNotBlank(jobConfig.getVersion()) && !jobConfig.isDryRun()) {
                    version = getProjectVersionFromScanStatus(services, scanSummaryList);
                    project = getProjectFromVersion(version, services);
                }
                // check the policy failures

                final HubReportGenerationInfo bomUpdateInfo = new HubReportGenerationInfo();
                bomUpdateInfo.setService(hubIntRestService);
                bomUpdateInfo.setProject(project);
                bomUpdateInfo.setVersion(version);
                bomUpdateInfo.setHostname(HostnameHelper.getMyHostname());
                bomUpdateInfo.setScanTargets(jobConfig.getScanTargetPaths());
                bomUpdateInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds());

                bomUpdateInfo.setBeforeScanTime(beforeScanTime);
                bomUpdateInfo.setAfterScanTime(afterScanTime);

                // bomUpdateInfo.setScanStatusDirectory(scanExecutor.getScanStatusDirectoryPath());

                boolean waitForBom = true;
                final boolean isGenRiskReport = taskConfigMap.getAsBoolean(HubScanParamEnum.GENERATE_RISK_REPORT.getKey());

                if (isGenRiskReport) {
                    generateRiskReport(taskContext, logger, bomUpdateInfo, scanSummaryList, hubSupport, services);
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
                        long timeout = hubConfig.getTimeout() * 1000;
                        waitForHub(scanStatusDataService, scanSummaryList, logger, timeout);
                    }
                    final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, hubIntRestService,
                            hubSupport, bomUpdateInfo, version.getLink(ProjectVersionItem.POLICY_STATUS_LINK));

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
        // logger.info("-> Generate Hub report : " +
        // jobConfig.isShouldGenerateRiskReport());
        final String formattedTime = String.format("%d minutes",
                TimeUnit.MILLISECONDS.toMinutes(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds()));
        logger.alwaysLog("-> Maximum wait time for the BOM Update : " + formattedTime);
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

    private ProjectVersionItem getProjectVersionFromScanStatus(HubServicesFactory services, List<ScanSummaryItem> scanSummaryList)
            throws IOException, InterruptedException, HubIntegrationException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
        CodeLocationItem codeLocationItem = services.createCodeLocationRestService()
                .getItem(scanSummaryList.get(0).getLink(ScanSummaryItem.CODE_LOCATION_LINK));
        String projectVersionUrl = codeLocationItem.getMappedProjectVersion();
        ProjectVersionItem projectVersion = services.createProjectVersionRestService().getItem(projectVersionUrl);
        return projectVersion;
    }

    private ProjectItem getProjectFromVersion(ProjectVersionItem version, HubServicesFactory services)
            throws IOException, BDRestException, URISyntaxException, UnexpectedHubResponseException {
        ProjectItem project = services.createProjectRestService().getItem(version.getLink(ProjectVersionItem.PROJECT_LINK));
        return project;
    }

    private void generateRiskReport(final TaskContext taskContext, final IntLogger logger,
            final HubReportGenerationInfo hubReportGenerationInfo, final List<ScanSummaryItem> scanSummaryList, final HubSupportHelper hubSupport,
            final HubServicesFactory services)
            throws IOException, BDRestException, URISyntaxException, InterruptedException, HubIntegrationException,
            UnexpectedHubResponseException, ProjectDoesNotExistException, MissingUUIDException {
        logger.info("Generating Risk Report");
        final long maximumWaitTime = hubReportGenerationInfo.getMaximumWaitTime();
        final ReportRestService reportService = services.createReportRestService(logger);
        final ScanStatusDataService scanStatusDataService = services.createScanStatusDataService();
        waitForHub(scanStatusDataService, scanSummaryList, logger, maximumWaitTime);

        // will wait for bom to be updated while generating the
        // report.
        final ReportCategoriesEnum[] categories = { ReportCategoriesEnum.VERSION, ReportCategoriesEnum.COMPONENTS };
        HubRiskReportData report = reportService.generateHubReport(hubReportGenerationInfo.getVersion(), ReportFormatEnum.JSON, categories);
        final String reportPath = taskContext.getWorkingDirectory() + File.separator
                + HubBambooUtils.HUB_RISK_REPORT_FILENAME;

        final Gson gson = new GsonBuilder().create();
        final String contents = gson.toJson(report);

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

    public void waitForHub(ScanStatusDataService scanStatusDataService, List<ScanSummaryItem> pendingScans, final IntLogger logger, final long timeout) {
        try {
            scanStatusDataService.assertBomImportScansFinished(pendingScans, timeout);
        } catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException | UnexpectedHubResponseException
                | HubIntegrationException | InterruptedException e) {
            logger.error(String.format("There was an error waiting for the scans: %s", e.getMessage()), e);
        }
    }
}
