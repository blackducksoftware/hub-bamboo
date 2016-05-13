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

import java.util.List;
import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobFieldEnum;

public class HubScanTaskConfigurator extends AbstractTaskConfigurator {

	@Override
	public Map<String, String> generateTaskConfigMap(final ActionParametersMap params,
			final TaskDefinition previousTaskDefinition) {
		final Map<String, String> configMap = super.generateTaskConfigMap(params, previousTaskDefinition);

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			configMap.put(key, params.getString(key));
		}

		// setGlobalConfigParameters(configMap);

		return configMap;
	}

	@Override
	public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);

		String key = HubScanParamEnum.PROJECT.getKey();
		final String project = params.getString(key);
		key = HubScanParamEnum.VERSION.getKey();
		final String version = params.getString(key);
		key = HubScanParamEnum.PHASE.getKey();
		final String phase = params.getString(key);
		key = HubScanParamEnum.DISTRIBUTION.getKey();
		final String distribution = params.getString(key);
		key = HubScanParamEnum.GENERATE_RISK_REPORT.getKey();
		final String generateRiskReport = params.getString(key);
		key = HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey();
		final String maxWaitTime = params.getString(key);
		key = HubScanParamEnum.SCANMEMORY.getKey();
		final String scanMemory = params.getString(key);
		key = HubScanParamEnum.TARGETS.getKey();
		final String scanTargetText = params.getString(key);

		final List<String> scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(scanTargetText, null);

		final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder(true);
		hubScanJobConfigBuilder.setProjectName(project);
		hubScanJobConfigBuilder.setVersion(version);
		hubScanJobConfigBuilder.setPhase(phase);
		hubScanJobConfigBuilder.setDistribution(distribution);
		hubScanJobConfigBuilder.setShouldGenerateRiskReport(generateRiskReport);
		hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTime);
		hubScanJobConfigBuilder.setScanMemory(scanMemory);
		hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
		hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = hubScanJobConfigBuilder.build();

		if (!result.isSuccess()) {

			checkValidationErrors(HubScanParamEnum.PROJECT, HubScanJobFieldEnum.PROJECT, result, errorCollection);
			checkValidationErrors(HubScanParamEnum.VERSION, HubScanJobFieldEnum.VERSION, result, errorCollection);
			checkValidationErrors(HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE, result, errorCollection);
			checkValidationErrors(HubScanParamEnum.SCANMEMORY, HubScanJobFieldEnum.SCANMEMORY, result, errorCollection);
			checkValidationErrors(HubScanParamEnum.TARGETS, HubScanJobFieldEnum.TARGETS, result, errorCollection);
		}
	}

	private void checkValidationErrors(final HubScanParamEnum parameter, final HubScanJobFieldEnum field,
			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final ErrorCollection errorCollection) {

		if (result.hasErrors(field)) {
			final String message = result.getResultString(field, ValidationResultEnum.ERROR);
			errorCollection.addError(parameter.getKey(), message);
		}
	}

	@Override
	public void populateContextForCreate(final Map<String, Object> context) {

		super.populateContextForCreate(context);

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			context.put(key, param.getDefaultValue());
		}
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

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {

			final String key = param.getKey();
			context.put(key, taskDefinition.getConfiguration().get(key));
		}
	}
}
