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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooPluginHelper;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

public class HubScanTask implements TaskType {

    private static final int DEFAULT_MAX_WAIT_TIME_MILLISEC = 5 * 60 * 1000;

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
            final HubBambooPluginHelper pluginHelper = new HubBambooPluginHelper(pluginAccessor);
            logger.alwaysLog("Initializing - Hub Bamboo Plugin : " + pluginHelper.getPluginVersion());
            final ConfigurationMap taskConfigMap = taskContext.getConfigurationMap();
            final HubServerConfig hubConfig = getHubServerConfig(logger);
            if (hubConfig == null) {
                logger.error("Please verify the correct dependent Hub configuration plugin is installed");
                logger.error("Please verify the configuration is correct if the plugin is installed.");
                result = resultBuilder.failedWithError().build();
                logTaskResult(logger, result);
                return result;
            }
            hubConfig.print(logger);

            final RestConnection restConnection = new CredentialsRestConnection(hubConfig);

            restConnection.connect();

            final HubServicesFactory services = new HubServicesFactory(restConnection);
            final CLIDataService cliDataService = services.createCLIDataService(logger);
            final File toolsDir = new File(HubBambooUtils.getInstance().getBambooHome(), CLI_FOLDER_NAME);

            final String thirdPartyVersion = BuildUtils.getCurrentVersion();
            final String pluginVersion = pluginHelper.getPluginVersion();
            final String shouldGenerateRiskReport = taskConfigMap.get(HubScanConfigFieldEnum.GENERATE_RISK_REPORT.getKey());
            final String maxWaitTimeForRiskReport = taskConfigMap.get(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey());
            boolean isRiskReportGenerated = false;
            long waitTimeForReport = DEFAULT_MAX_WAIT_TIME_MILLISEC;

            if (StringUtils.isNotBlank(shouldGenerateRiskReport)) {
                isRiskReportGenerated = Boolean.valueOf(shouldGenerateRiskReport);
            }

            if (StringUtils.isNotBlank(maxWaitTimeForRiskReport)) {
                waitTimeForReport = NumberUtils.toInt(maxWaitTimeForRiskReport) * 60 * 1000; // 5 minutes is the default
            }
            final HubScanConfig hubScanConfig = getScanConfig(taskConfigMap, taskContext.getWorkingDirectory(), toolsDir, thirdPartyVersion, pluginVersion,
                    logger);

            final boolean isFailOnPolicySelected = taskConfigMap
                    .getAsBoolean(HubScanConfigFieldEnum.FAIL_ON_POLICY_VIOLATION.getKey());

            List<ScanSummaryItem> scanSummaryList = null;
            try {
                scanSummaryList = cliDataService.installAndRunScan(hubConfig, hubScanConfig);

            } catch (final ScanFailedException e) {
                if (resultBuilder.getTaskState() != TaskState.SUCCESS) {
                    logger.error("Hub Scan Failed : " + e.getMessage());
                    result = resultBuilder.build();
                    logTaskResult(logger, result);
                    return result;
                }
            }
            if (!hubScanConfig.isDryRun()) {
                if (isRiskReportGenerated || isFailOnPolicySelected) {
                    services.createScanStatusDataService(logger).assertBomImportScansFinished(scanSummaryList,
                            waitTimeForReport);
                }
                if (isRiskReportGenerated) {
                    final SecureToken token = SecureToken.createFromString(taskContext.getRuntimeTaskContext().get(HubBambooUtils.HUB_TASK_SECURE_TOKEN));
                    publishRiskReportFiles(logger, taskContext, token, services.createRiskReportDataService(logger), hubScanConfig.getProjectName(),
                            hubScanConfig.getVersion());
                }
                if (isFailOnPolicySelected) {
                    final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, services, hubScanConfig.getProjectName(),
                            hubScanConfig.getVersion(),
                            hubScanConfig.isDryRun());

                    result = policyResult.build();
                }
            } else {
                if (isRiskReportGenerated) {
                    logger.warn("Will not generate the risk report because this was a dry run scan.");
                }
                if (isFailOnPolicySelected) {
                    logger.warn("Will not run the Failure conditions because this was a dry run scan.");
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

    private TaskResultBuilder checkPolicyFailures(final TaskResultBuilder resultBuilder, final TaskContext taskContext,
            final IntLogger logger, final HubServicesFactory services, String projectName, String versionName, final boolean isDryRun) {
        try {

            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return resultBuilder.success();
            }
            final PolicyStatusDataService policyStatusDataService = services.createPolicyStatusDataService(logger);

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
        } catch (final HubIntegrationException e) {
            logger.error(e.getMessage(), e);
            return resultBuilder.failed();
        }
    }

    private HubServerConfig getHubServerConfig(final IntLogger logger)
            throws IllegalArgumentException, EncryptionException {

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

        try {
            final HubServerConfig result = HubBambooUtils.getInstance()
                    .buildConfigFromStrings(hubUrl, hubUser, hubPass, hubPassLength, hubProxyUrl, hubProxyPort,
                            hubProxyNoHost, hubProxyUser, hubProxyPass, hubProxyPassLength);
            return result;

        } catch (final IllegalStateException ex) {
            logger.error("Hub Server Configuration Invalid.", ex);
        }

        return null;
    }

    private HubScanConfig getScanConfig(final ConfigurationMap configMap, final File workingDirectory, final File toolsDir,
            String thirdPartyVersion, String pluginVersion,
            final IntLogger logger) throws HubIntegrationException, IOException {
        final String project = configMap.get(HubScanConfigFieldEnum.PROJECT.getKey());
        final String version = configMap.get(HubScanConfigFieldEnum.VERSION.getKey());
        final String phase = configMap.get(HubScanConfigFieldEnum.PHASE.getKey());
        final String distribution = configMap.get(HubScanConfigFieldEnum.DISTRIBUTION.getKey());

        final String dryRun = configMap.get(HubScanConfigFieldEnum.DRY_RUN.getKey());

        final String scanMemory = configMap.get(HubScanConfigFieldEnum.SCANMEMORY.getKey());
        final String targets = configMap.get(HubScanConfigFieldEnum.TARGETS.getKey());

        final List<String> scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(targets, workingDirectory);

        if (scanTargets.isEmpty()) {
            // no targets specified assume the working directory.
            scanTargets.add(workingDirectory.getCanonicalPath());
        }

        try {
            final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
            hubScanConfigBuilder.setProjectName(project);
            hubScanConfigBuilder.setVersion(version);
            hubScanConfigBuilder.setPhase(PhaseEnum.getPhaseByDisplayValue(phase).name());
            hubScanConfigBuilder.setDistribution(DistributionEnum.getDistributionByDisplayValue(distribution).name());
            hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
            hubScanConfigBuilder.setDryRun(Boolean.valueOf(dryRun));
            hubScanConfigBuilder.setScanMemory(scanMemory);
            hubScanConfigBuilder.addAllScanTargetPaths(scanTargets);
            hubScanConfigBuilder.setToolsDir(toolsDir);
            hubScanConfigBuilder.setThirdPartyVersion(thirdPartyVersion);
            hubScanConfigBuilder.setPluginVersion(pluginVersion);
            return hubScanConfigBuilder.build();
        } catch (final IllegalStateException ex) {
            logger.error("Hub Scan Configuration Invalid.", ex);
        }

        return null;
    }

    private String getPersistedValue(final String key) {
        return (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, key);
    }

    private void publishRiskReportFiles(final IntLogger logger, TaskContext taskContext, SecureToken token, RiskReportDataService riskReportDataService,
            String projectName, String projectVersion) {

        final BuildContext buildContext = taskContext.getBuildContext();
        final PlanResultKey planResultKey = buildContext.getPlanResultKey();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        try {
            final File baseDirectory = new File(taskContext.getWorkingDirectory() + HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME);
            riskReportDataService.createRiskReportFiles(baseDirectory, projectName, projectVersion);
            final Map<String, String> config = new HashMap<>();
            final ArtifactDefinitionContext artifact = createArtifactDefContext(token);
            final ArtifactPublishingResult publishResult = artifactManager.publish(buildLogger, planResultKey,
                    baseDirectory, artifact, config, RISK_REPORT_MINIMUM_FILE_COUNT);

            if (!publishResult.shouldContinueBuild()) {
                logger.error("Could not publish the artifacts for the Risk Report");
            }
        } catch (final HubIntegrationException ex) {
            logger.error("Could not publish the Risk Report", ex);
        }
    }

    private ArtifactDefinitionContextImpl createArtifactDefContext(SecureToken token) {
        final ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl(
                HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME, false, token);
        artifact.setCopyPattern("**/*");
        return artifact;
    }
}
