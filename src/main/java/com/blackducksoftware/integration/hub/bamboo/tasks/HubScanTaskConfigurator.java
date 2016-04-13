package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.job.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.logging.IntBufferedLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

public class HubScanTaskConfigurator extends AbstractTaskConfigurator {

	@Override
	public Map<String, String> generateTaskConfigMap(final ActionParametersMap params,
			final TaskDefinition previousTaskDefinition) {
		final Map<String, String> configMap = super.generateTaskConfigMap(params, previousTaskDefinition);

		String key = HubScanParamEnum.PROJECT.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.VERSION.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.PHASE.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.DISTRIBUTION.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.GENERATE_RISK_REPORT.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.SCANMEMORY.getKey();
		configMap.put(key, params.getString(key));
		key = HubScanParamEnum.TARGETS.getKey();
		configMap.put(key, params.getString(key));

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

		final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder();
		hubScanJobConfigBuilder.setProjectName(project);
		hubScanJobConfigBuilder.setVersion(version);
		hubScanJobConfigBuilder.setPhase(phase);
		hubScanJobConfigBuilder.setDistribution(distribution);
		hubScanJobConfigBuilder.setShouldGenerateRiskReport(generateRiskReport);
		hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTime);
		hubScanJobConfigBuilder.setScanMemory(scanMemory);
		hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
		hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();

		try {
			final IntBufferedLogger bufferedLogger = new IntBufferedLogger();

			if (!hubScanJobConfigBuilder.validateProjectAndVersion(bufferedLogger)) {

				if (!bufferedLogger.getOutputList(LogLevel.ERROR).isEmpty()) {
					// project or version is the cause of the error

					if (!hubScanJobConfigBuilder.validateProject(bufferedLogger)) {
						checkValidationErrors(HubScanParamEnum.PROJECT, bufferedLogger, errorCollection);
					}

					if (!hubScanJobConfigBuilder.validateVersion(bufferedLogger)) {
						checkValidationErrors(HubScanParamEnum.VERSION, bufferedLogger, errorCollection);
					}
				}
			}

			if (!hubScanJobConfigBuilder.validateMaxWaitTimeForBomUpdate(bufferedLogger)) {
				checkValidationErrors(HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE, bufferedLogger, errorCollection);
			}

			if (!hubScanJobConfigBuilder.validateScanMemory(bufferedLogger)) {
				checkValidationErrors(HubScanParamEnum.SCANMEMORY, bufferedLogger, errorCollection);
			}

			if (!hubScanJobConfigBuilder.validateScanTargetPaths(bufferedLogger)) {
				checkValidationErrors(HubScanParamEnum.TARGETS, bufferedLogger, errorCollection);
			}
		} catch (final IOException ex) {

		}
	}

	private void checkValidationErrors(final HubScanParamEnum parameter, final IntBufferedLogger logger,
			final ErrorCollection errorCollection) {

		final List<String> errorList = logger.getOutputList(LogLevel.ERROR);

		for (final String error : errorList) {
			errorCollection.addError(parameter.getKey(), error);
		}
		// finished checking for errors so reset the logger.
		logger.resetAllLogs();
	}

	@Override
	public void populateContextForCreate(final Map<String, Object> context) {

		super.populateContextForCreate(context);
		String key = HubScanParamEnum.PROJECT.getKey();
		context.put(key, "");
		key = HubScanParamEnum.VERSION.getKey();
		context.put(key, "");
		key = HubScanParamEnum.PHASE.getKey();
		context.put(key, PhaseEnum.PLANNING.getDisplayValue());
		key = HubScanParamEnum.DISTRIBUTION.getKey();
		context.put(key, DistributionEnum.EXTERNAL.getDisplayValue());
		key = HubScanParamEnum.GENERATE_RISK_REPORT.getKey();
		context.put(key, "false");
		key = HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey();
		context.put(key, "5");
		key = HubScanParamEnum.SCANMEMORY.getKey();
		context.put(key, "4096");
		key = HubScanParamEnum.TARGETS.getKey();
		context.put(key, "");
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
		String key = HubScanParamEnum.PROJECT.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.VERSION.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.PHASE.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.DISTRIBUTION.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.GENERATE_RISK_REPORT.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.SCANMEMORY.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.TARGETS.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
	}
}
