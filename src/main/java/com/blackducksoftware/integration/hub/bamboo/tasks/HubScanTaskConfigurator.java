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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionPhaseEnum;
import com.blackducksoftware.integration.hub.request.validator.ProjectRequestValidator;
import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.hub.validator.HubScanConfigValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubScanTaskConfigurator extends AbstractTaskConfigurator {

    private static final String PHASES = "hubPhases";

    private static final String DISTRIBUTIONS = "hubDistributions";

    @Override
    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params,
            final TaskDefinition previousTaskDefinition) {
        final Map<String, String> configMap = super.generateTaskConfigMap(params, previousTaskDefinition);

        for (final HubScanConfigFieldEnum param : HubScanConfigFieldEnum.values()) {
            final String key = param.getKey();
            final String value = params.getString(key);
            configMap.put(key, value);
        }
        return configMap;
    }

    @Override
    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        final String projectName = params.getString(HubScanConfigFieldEnum.PROJECT.getKey());
        final String versionName = params.getString(HubScanConfigFieldEnum.VERSION.getKey());
        final String phase = params.getString(HubScanConfigFieldEnum.PHASE.getKey());
        final String distribution = params.getString(HubScanConfigFieldEnum.DISTRIBUTION.getKey());
        final String scanMemory = params.getString(HubScanConfigFieldEnum.SCANMEMORY.getKey());
        final String bomWaitTime = params.getString(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey());
        final String scanTargetText = params.getString(HubScanConfigFieldEnum.TARGETS.getKey());
        final String excludePatternsString = params.getString(HubScanConfigFieldEnum.EXCLUDE_PATTERNS.getKey());

        List<String> scanTargets = new ArrayList<>();
        try {
            scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(scanTargetText, null);
        } catch (final IOException e) {
            errorCollection.addError(HubScanConfigFieldEnum.TARGETS.getKey(), e.getMessage());
        }

        final String[] excludePatterns = HubBambooUtils.getInstance().createExcludePatterns(excludePatternsString);

        final ProjectRequestValidator projectRequestValidator = new ProjectRequestValidator();
        projectRequestValidator.setProjectName(projectName);
        projectRequestValidator.setVersionName(versionName);
        projectRequestValidator.setPhase(phase);
        projectRequestValidator.setDistribution(distribution);
        final ValidationResults projectRequesResult = projectRequestValidator.assertValid();
        if (!projectRequesResult.isSuccess()) {
            checkValidationErrors(HubScanConfigFieldEnum.PROJECT, projectRequesResult, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.VERSION, projectRequesResult, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.PHASE, projectRequesResult, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.DISTRIBUTION, projectRequesResult, errorCollection);
        }
        final HubScanConfigValidator hubScanJobConfigValidator = new HubScanConfigValidator();
        hubScanJobConfigValidator.setScanMemory(scanMemory);
        hubScanJobConfigValidator.addAllScanTargetPaths(new HashSet<>(scanTargets));
        hubScanJobConfigValidator.disableScanTargetPathExistenceCheck();
        final ValidationResults scanConfigResult = hubScanJobConfigValidator.assertValid();
        validateExcludePatterns(scanConfigResult, excludePatterns);
        if (!scanConfigResult.isSuccess()) {
            checkValidationErrors(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE, scanConfigResult, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.SCANMEMORY, scanConfigResult, errorCollection);
            if (!scanTargets.isEmpty()) {
                checkValidationErrors(HubScanConfigFieldEnum.TARGETS, scanConfigResult, errorCollection);
            }
        }
        checkValidationErrors(HubScanConfigFieldEnum.EXCLUDE_PATTERNS, scanConfigResult, errorCollection);
        checkBomWaitTime(bomWaitTime, errorCollection);
    }

    private void validateExcludePatterns(final ValidationResults result, final String[] excludePatterns) {
        if (excludePatterns == null || excludePatterns.length == 0) {
            return;
        }

        for (final String excludePattern : excludePatterns) {
            validateExcludePattern(result, excludePattern);
        }
    }

    private void validateExcludePattern(final ValidationResults result, final String excludePattern) {
        if (StringUtils.isNotBlank(excludePattern)) {
            if (!excludePattern.startsWith("/")) {
                result.addResult(HubScanConfigFieldEnum.EXCLUDE_PATTERNS,
                        new ValidationResult(ValidationResultEnum.WARN, "The exclusion pattern : " + excludePattern + " must start with a /."));
            }
            if (!excludePattern.endsWith("/")) {
                result.addResult(HubScanConfigFieldEnum.EXCLUDE_PATTERNS,
                        new ValidationResult(ValidationResultEnum.WARN, "The exclusion pattern : " + excludePattern + " must end with a /."));
            }
            if (excludePattern.contains("**")) {
                result.addResult(HubScanConfigFieldEnum.EXCLUDE_PATTERNS,
                        new ValidationResult(ValidationResultEnum.WARN, " The exclusion pattern : " + excludePattern + " can not contain **."));
            }
        }
    }

    private void checkBomWaitTime(final String bomWaitTime, final ErrorCollection errorCollection) {
        if (StringUtils.isBlank(bomWaitTime)) {
            return;
        }
        int bomWaitTimeInt = 0;
        try {
            final String integerString = StringUtils.trimToNull(bomWaitTime);
            if (integerString != null) {
                try {
                    bomWaitTimeInt = Integer.valueOf(integerString);
                } catch (final NumberFormatException e) {
                    errorCollection.addError(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(),
                            "The String : " + bomWaitTime + " , is not an Integer.");
                    return;
                }
            } else {
                errorCollection.addError(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(), "The String : " + bomWaitTime + " , is not an Integer.");
            }
        } catch (final IllegalArgumentException e) {
            errorCollection.addError(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(), e.getMessage());
            return;
        }
        if (bomWaitTimeInt <= 0) {
            errorCollection.addError(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(),
                    "The maximum wait time for the BOM Update must be greater than 0.");
        }
    }

    private void checkValidationErrors(final HubScanConfigFieldEnum field,
            final ValidationResults result,
            final ErrorCollection errorCollection) {

        if (result.hasErrors()) {
            final String message = result.getResultString(field);
            errorCollection.addError(field.getKey(), message);
        }
    }

    @Override
    public void populateContextForCreate(final Map<String, Object> context) {

        super.populateContextForCreate(context);

        context.put(HubScanConfigFieldEnum.PROJECT.getKey(), "");
        context.put(HubScanConfigFieldEnum.VERSION.getKey(), "");
        context.put(HubScanConfigFieldEnum.PHASE.getKey(), "");
        context.put(PHASES, getHubPhases());
        context.put(HubScanConfigFieldEnum.DISTRIBUTION.getKey(), "");
        context.put(DISTRIBUTIONS, getHubDistributions());
        context.put(HubScanConfigFieldEnum.PROJECT_LEVEL_ADJUSTMENTS.getKey(), "true");
        context.put(HubScanConfigFieldEnum.GENERATE_RISK_REPORT.getKey(), "false");
        context.put(HubScanConfigFieldEnum.DRY_RUN.getKey(), "false");
        context.put(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(),
                String.valueOf(HubScanConfigValidator.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES));
        context.put(HubScanConfigFieldEnum.SCANMEMORY.getKey(), String.valueOf(HubScanConfigValidator.DEFAULT_MEMORY_IN_MEGABYTES));
        context.put(HubScanConfigFieldEnum.CODE_LOCATION_ALIAS.getKey(), "");
        context.put(HubScanConfigFieldEnum.TARGETS.getKey(), "");
        context.put(HubScanConfigFieldEnum.FAIL_ON_POLICY_VIOLATION.getKey(), "false");
    }

    @Override
    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition) {

        super.populateContextForEdit(context, taskDefinition);
        populateContextMap(context, taskDefinition);
    }

    @Override
    public void populateContextForView(final Map<String, Object> context, final TaskDefinition taskDefinition) {

        super.populateContextForView(context, taskDefinition);
        populateContextMap(context, taskDefinition);
    }

    private Map<String, String> getHubPhases() {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(ProjectVersionPhaseEnum.PLANNING.toString(), "In Planning");
        map.put(ProjectVersionPhaseEnum.DEVELOPMENT.toString(), "In Development");
        map.put(ProjectVersionPhaseEnum.RELEASED.toString(), "Released");
        map.put(ProjectVersionPhaseEnum.DEPRECATED.toString(), "Deprecated");
        map.put(ProjectVersionPhaseEnum.ARCHIVED.toString(), "Archived");
        return map;
    }

    private Map<String, String> getHubDistributions() {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(ProjectVersionDistributionEnum.EXTERNAL.toString(), "External");
        map.put(ProjectVersionDistributionEnum.SAAS.toString(), "SaaS");
        map.put(ProjectVersionDistributionEnum.INTERNAL.toString(), "Internal");
        map.put(ProjectVersionDistributionEnum.OPENSOURCE.toString(), "Open Source");
        return map;
    }

    private void populateContextMap(final Map<String, Object> context, final TaskDefinition taskDefinition) {

        for (final HubScanConfigFieldEnum param : HubScanConfigFieldEnum.values()) {
            final String key = param.getKey();
            context.put(key, taskDefinition.getConfiguration().get(key));
        }
        context.put(PHASES, getHubPhases());
        context.put(DISTRIBUTIONS, getHubDistributions());
    }
}
