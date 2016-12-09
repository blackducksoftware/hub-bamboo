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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.hub.validator.HubScanConfigValidator;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubScanTaskConfigurator extends AbstractTaskConfigurator {

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

        final String project = params.getString(HubScanConfigFieldEnum.PROJECT.getKey());
        final String version = params.getString(HubScanConfigFieldEnum.VERSION.getKey());
        final String scanMemory = params.getString(HubScanConfigFieldEnum.SCANMEMORY.getKey());
        final String scanTargetText = params.getString(HubScanConfigFieldEnum.TARGETS.getKey());

        List<String> scanTargets = new ArrayList<>();
        try {
            scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(scanTargetText, null);
        } catch (IOException e) {
            errorCollection.addError(HubScanConfigFieldEnum.TARGETS.getKey(), e.getMessage());
        }

        final HubScanConfigValidator hubScanJobConfigBuilder = new HubScanConfigValidator();
        hubScanJobConfigBuilder.setProjectName(project);
        hubScanJobConfigBuilder.setVersion(version);
        hubScanJobConfigBuilder.setScanMemory(scanMemory);
        hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
        hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();
        final ValidationResults result = hubScanJobConfigBuilder.assertValid();

        if (!result.isSuccess()) {

            checkValidationErrors(HubScanConfigFieldEnum.PROJECT, result, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.VERSION, result, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE, result, errorCollection);
            checkValidationErrors(HubScanConfigFieldEnum.SCANMEMORY, result, errorCollection);
            if (!scanTargets.isEmpty()) {
                checkValidationErrors(HubScanConfigFieldEnum.TARGETS, result, errorCollection);
            }
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
        context.put(HubScanConfigFieldEnum.PHASE.getKey(), PhaseEnum.PLANNING.getDisplayValue());
        context.put(HubScanConfigFieldEnum.DISTRIBUTION.getKey(), DistributionEnum.EXTERNAL.getDisplayValue());
        context.put(HubScanConfigFieldEnum.GENERATE_RISK_REPORT.getKey(), "false");
        context.put(HubScanConfigFieldEnum.DRY_RUN.getKey(), "false");
        context.put(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(),
                String.valueOf(HubScanConfigValidator.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES));
        context.put(HubScanConfigFieldEnum.SCANMEMORY.getKey(), String.valueOf(HubScanConfigValidator.DEFAULT_MEMORY_IN_MEGABYTES));
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

    private void populateContextMap(final Map<String, Object> context, final TaskDefinition taskDefinition) {

        for (final HubScanConfigFieldEnum param : HubScanConfigFieldEnum.values()) {

            final String key = param.getKey();
            context.put(key, taskDefinition.getConfiguration().get(key));
        }
    }
}
