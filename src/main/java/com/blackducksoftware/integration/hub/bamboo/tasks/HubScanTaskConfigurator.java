package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.util.Map;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;

public class HubScanTaskConfigurator extends AbstractTaskConfigurator {

	@Override
	public Map<String, String> generateTaskConfigMap(final ActionParametersMap params,
			final TaskDefinition previousTaskDefinition) {
		return super.generateTaskConfigMap(params, previousTaskDefinition);
	}

	@Override
	public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
		super.validate(params, errorCollection);
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
