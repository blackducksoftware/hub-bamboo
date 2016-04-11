package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.util.List;
import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.job.HubScanJobConfigBuilder;

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
		key = HubScanParamEnum.SCANMEMORY.getKey();
		final String scanMemory = params.getString(key);
		key = HubScanParamEnum.TARGETS.getKey();
		final String scanTargetText = params.getString(key);

		final List<String> scanTargets = HubBambooUtils.getInstance().createScanTargetPaths(scanTargetText);

		final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder();
		hubScanJobConfigBuilder.setProjectName(project);
		hubScanJobConfigBuilder.setVersion(version);
		hubScanJobConfigBuilder.setPhase(phase);
		hubScanJobConfigBuilder.setDistribution(distribution);
		hubScanJobConfigBuilder.setWorkingDirectory("/");
		hubScanJobConfigBuilder.setShouldGenerateRiskReport(false);
		// hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(5);
		hubScanJobConfigBuilder.setScanMemory(scanMemory);
		hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
		hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();
	}

	@Override
	public void populateContextForCreate(final Map<String, Object> context) {

		super.populateContextForCreate(context);
		String key = HubScanParamEnum.PROJECT.getKey();
		context.put(key, "");
		key = HubScanParamEnum.VERSION.getKey();
		context.put(key, "");
		key = HubScanParamEnum.PHASE.getKey();
		context.put(key, "In Planning");
		key = HubScanParamEnum.DISTRIBUTION.getKey();
		context.put(key, "External");
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
		key = HubScanParamEnum.SCANMEMORY.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
		key = HubScanParamEnum.TARGETS.getKey();
		context.put(key, taskDefinition.getConfiguration().get(key));
	}
}
