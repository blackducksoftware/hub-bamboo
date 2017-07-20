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
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.bamboo.util.BuildUtils;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.PluginAccessor;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooPluginHelper;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.exception.ScanFailedException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.enumeration.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.request.builder.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
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

    public HubScanTask(final EnvironmentVariableAccessor environmentVariableAccessor, final BandanaManager bandanaManager, final ArtifactManager artifactManager, final PluginAccessor pluginAccessor) {
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
        final Map<String, String> envVars = HubBambooUtils.getInstance().getEnvironmentVariablesMap(environmentVariableAccessor.getEnvironment(), environmentVariableAccessor.getEnvironment(taskContext));

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
            final RestConnection restConnection = HubBambooUtils.getInstance().getRestConnection(logger, hubConfig);
            final HubServicesFactory services = new HubServicesFactory(restConnection);
            services.addEnvironmentVariables(envVars);
            final MetaService metaService = services.createMetaService(logger);
            final CLIDataService cliDataService = services.createCLIDataService(logger, hubConfig.getTimeout() * 60 * 1000);
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
                waitTimeForReport = NumberUtils.toInt(maxWaitTimeForRiskReport);
                if (waitTimeForReport <= 0) {
                    // 5 minutes is the default
                    waitTimeForReport = 5 * 60 * 1000;
                } else {
                    waitTimeForReport = waitTimeForReport * 60 * 1000;
                }
            } else {
                waitTimeForReport = 5 * 60 * 1000;
            }
            final HubScanConfig hubScanConfig = getScanConfig(taskConfigMap, taskContext.getWorkingDirectory(), toolsDir, logger);
            if (hubScanConfig == null) {
                result = resultBuilder.failedWithError().build();
                logTaskResult(logger, result);
                return result;
            }
            final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
            projectRequestBuilder.setProjectName(taskConfigMap.get(HubScanConfigFieldEnum.PROJECT.getKey()).trim());
            projectRequestBuilder.setVersionName(taskConfigMap.get(HubScanConfigFieldEnum.VERSION.getKey()).trim());
            projectRequestBuilder.setPhase(taskConfigMap.get(HubScanConfigFieldEnum.PHASE.getKey()));
            projectRequestBuilder.setDistribution(taskConfigMap.get(HubScanConfigFieldEnum.DISTRIBUTION.getKey()));
            projectRequestBuilder.setProjectLevelAdjustments(taskConfigMap.getAsBoolean(HubScanConfigFieldEnum.PROJECT_LEVEL_ADJUSTMENTS.getKey()));

            final boolean isFailOnPolicySelected = taskConfigMap.getAsBoolean(HubScanConfigFieldEnum.FAIL_ON_POLICY_VIOLATION.getKey());

            ProjectVersionView version = null;
            try {
                version = cliDataService.installAndRunControlledScan(hubConfig, hubScanConfig, projectRequestBuilder.build(), isRiskReportGenerated || isFailOnPolicySelected,
                        new IntegrationInfo(ThirdPartyName.BAMBOO.getName(), thirdPartyVersion, pluginVersion));
            } catch (final ScanFailedException e) {
                logger.error("Hub Scan Failed : " + e.getMessage());
                result = resultBuilder.failedWithError().build();
                logTaskResult(logger, result);
                return result;
            }
            if (!hubScanConfig.isDryRun()) {
                ProjectView project = null;

                if (isRiskReportGenerated || isFailOnPolicySelected) {
                    project = getProjectFromVersion(services.createProjectRequestService(logger), metaService, version);
                }
                if (isRiskReportGenerated) {
                    final SecureToken token = SecureToken.createFromString(taskContext.getRuntimeTaskContext().get(HubBambooUtils.HUB_TASK_SECURE_TOKEN));
                    publishRiskReportFiles(logger, taskContext, token, services.createRiskReportDataService(logger, waitTimeForReport), project, version);
                }
                if (isFailOnPolicySelected) {
                    final TaskResultBuilder policyResult = checkPolicyFailures(resultBuilder, taskContext, logger, services, metaService, version, hubScanConfig.isDryRun());

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

    private ProjectView getProjectFromVersion(final ProjectRequestService projectRequestService, final MetaService metaService, final ProjectVersionView version) throws IntegrationException {
        final String projectURL = metaService.getFirstLink(version, MetaService.PROJECT_LINK);
        final ProjectView projectVersion = projectRequestService.getItem(projectURL, ProjectView.class);
        return projectVersion;
    }

    private TaskResultBuilder checkPolicyFailures(final TaskResultBuilder resultBuilder, final TaskContext taskContext, final IntLogger logger, final HubServicesFactory services, final MetaService metaService,
            final ProjectVersionView version, final boolean isDryRun) {
        try {
            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return resultBuilder.success();
            }
            final String policyStatusLink = metaService.getFirstLink(version, MetaService.POLICY_STATUS_LINK);

            final VersionBomPolicyStatusView policyStatusItem = services.createHubResponseService().getItem(policyStatusLink, VersionBomPolicyStatusView.class);
            if (policyStatusItem == null) {
                logger.error("Could not find any information about the Policy status of the bom.");
                return resultBuilder.failed();
            }

            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
            final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
            if (policyStatusItem.overallStatus == VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION) {
                logger.error(policyStatusMessage);
                return resultBuilder.failedWithError();
            }
            logger.info(policyStatusMessage);
            return resultBuilder.success();
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return resultBuilder.failed();
        }
    }

    private HubServerConfig getHubServerConfig(final IntLogger logger) {
        try {
            final String hubUrl = getPersistedValue(HubConfigKeys.CONFIG_HUB_URL);
            final String hubUser = getPersistedValue(HubConfigKeys.CONFIG_HUB_USER);
            final String hubPass = getPersistedValue(HubConfigKeys.CONFIG_HUB_PASS);
            final String hubPassLength = getPersistedValue(HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
            final String hubImportSSLCerts = getPersistedValue(HubConfigKeys.CONFIG_HUB_IMPORT_SSL_CERTIFICATES);
            final String hubProxyUrl = getPersistedValue(HubConfigKeys.CONFIG_PROXY_HOST);
            final String hubProxyPort = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PORT);
            final String hubProxyNoHost = getPersistedValue(HubConfigKeys.CONFIG_PROXY_NO_HOST);
            final String hubProxyUser = getPersistedValue(HubConfigKeys.CONFIG_PROXY_USER);
            final String hubProxyPass = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PASS);
            final String hubProxyPassLength = getPersistedValue(HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

            final HubServerConfig result = HubBambooUtils.getInstance().buildConfigFromStrings(hubUrl, hubUser, hubPass, hubPassLength, hubImportSSLCerts, hubProxyUrl, hubProxyPort, hubProxyNoHost, hubProxyUser, hubProxyPass,
                    hubProxyPassLength);
            return result;
        } catch (final IllegalStateException e) {
            logger.error(e.getMessage());
            logger.debug("", e);
        }
        return null;
    }

    private HubScanConfig getScanConfig(final ConfigurationMap configMap, final File workingDirectory, final File toolsDir, final IntLogger logger) throws IOException {
        try {
            final String dryRun = configMap.get(HubScanConfigFieldEnum.DRY_RUN.getKey());
            final String cleanupLogsOnSuccess = configMap.get(HubScanConfigFieldEnum.CLEANUP_LOGS_ON_SUCCESS.getKey());

            final String unmapPreviousCodeLocations = configMap.get(HubScanConfigFieldEnum.UNMAP_PREVIOUS_CODE_LOCATIONS.getKey());
            final String deletePreviousCodeLocations = configMap.get(HubScanConfigFieldEnum.DELETE_PREVIOUS_CODE_LOCATIONS.getKey());

            final String excludePatternsConfig = configMap.get(HubScanConfigFieldEnum.EXCLUDE_PATTERNS.getKey());

            final String[] excludePatterns = HubBambooUtils.getInstance().createExcludePatterns(excludePatternsConfig);

            final String scanMemory = configMap.get(HubScanConfigFieldEnum.SCANMEMORY.getKey());
            final String codeLocationName = configMap.get(HubScanConfigFieldEnum.CODE_LOCATION_ALIAS.getKey()).trim();
            final String targets = configMap.get(HubScanConfigFieldEnum.TARGETS.getKey());

            final String hubWorkspaceCheckString = getPersistedValue(HubConfigKeys.CONFIG_HUB_WORKSPACE_CHECK);

            Boolean hubWorkspaceCheck = true;
            if (StringUtils.isNotBlank(hubWorkspaceCheckString)) {
                hubWorkspaceCheck = Boolean.valueOf(hubWorkspaceCheckString);
            }

            final List<String> scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(targets, workingDirectory);

            if (scanTargets.isEmpty()) {
                // no targets specified assume the working directory.
                scanTargets.add(workingDirectory.getCanonicalPath());
            }

            final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
            hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
            hubScanConfigBuilder.setDryRun(Boolean.valueOf(dryRun));
            hubScanConfigBuilder.setCleanupLogsOnSuccess(Boolean.valueOf(cleanupLogsOnSuccess));
            hubScanConfigBuilder.setScanMemory(scanMemory);
            hubScanConfigBuilder.addAllScanTargetPaths(scanTargets);
            hubScanConfigBuilder.setExcludePatterns(excludePatterns);
            hubScanConfigBuilder.setToolsDir(toolsDir);
            hubScanConfigBuilder.setCodeLocationAlias(codeLocationName);
            hubScanConfigBuilder.setUnmapPreviousCodeLocations(Boolean.valueOf(unmapPreviousCodeLocations));
            hubScanConfigBuilder.setDeletePreviousCodeLocations(Boolean.valueOf(deletePreviousCodeLocations));
            if (hubWorkspaceCheck) {
                hubScanConfigBuilder.enableScanTargetPathsWithinWorkingDirectoryCheck();
            }
            return hubScanConfigBuilder.build();
        } catch (final IllegalStateException e) {
            logger.error(e.getMessage());
            logger.debug("", e);
        }
        return null;
    }

    private String getPersistedValue(final String key) {
        return (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, key);
    }

    private void publishRiskReportFiles(final IntLogger logger, final TaskContext taskContext, final SecureToken token, final RiskReportDataService riskReportDataService, final ProjectView project, final ProjectVersionView version) {

        final BuildContext buildContext = taskContext.getBuildContext();
        final PlanResultKey planResultKey = buildContext.getPlanResultKey();
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        try {
            final File baseDirectory = new File(taskContext.getWorkingDirectory(), HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME);
            riskReportDataService.createReportFiles(baseDirectory, project, version);
            final Map<String, String> config = new HashMap<>();
            final ArtifactDefinitionContext artifact = createArtifactDefContext(token);
            final ArtifactPublishingResult publishResult = artifactManager.publish(buildLogger, planResultKey, baseDirectory, artifact, config, RISK_REPORT_MINIMUM_FILE_COUNT);
            if (!publishResult.shouldContinueBuild()) {
                logger.error("Could not publish the artifacts for the Risk Report");
            }
            cleanupReportFiles(baseDirectory);
        } catch (final IntegrationException ex) {
            logger.error("Could not publish the Risk Report", ex);
        }
    }

    private void cleanupReportFiles(final File file) {
        if (file.isDirectory()) {
            for (final File subFile : file.listFiles()) {
                cleanupReportFiles(subFile);
            }
        }
        file.delete();
    }

    private ArtifactDefinitionContextImpl createArtifactDefContext(final SecureToken token) {
        final ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl(HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME, false, token);
        artifact.setCopyPattern("**/*");
        return artifact;
    }
}
