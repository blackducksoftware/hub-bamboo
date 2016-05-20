package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.security.SecureTokenService;
import com.atlassian.bamboo.task.RuntimeTaskDataProvider;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.agent.messages.AuthenticableMessage;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class HubTaskDataProvider implements RuntimeTaskDataProvider {

	private SecureTokenService tokenService;
	private ArtifactDefinitionManager artifactDefManager;

	public Map<String, String> populateRuntimeTaskData(final TaskDefinition taskDef, final CommonContext context) {
		final Map<String, String> map = new HashMap<String, String>();
		final SecureToken token = tokenService
				.generate(AuthenticableMessage.Identification.forResultKey(context.getResultKey()));
		map.put(HubBambooUtils.HUB_TASK_SECURE_TOKEN, token.getToken());

		return map;
	}

	public void processRuntimeTaskData(final TaskDefinition taskDef, final CommonContext context) {
	}

	public void setSecureTokenService(final SecureTokenService tokenService) {
		this.tokenService = tokenService;
	}

	public void setArtifactDefinitionManager(final ArtifactDefinitionManager artifactDefManager) {
		this.artifactDefManager = artifactDefManager;
	}
}
