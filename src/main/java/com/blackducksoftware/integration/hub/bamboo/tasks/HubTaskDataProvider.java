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

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.security.SecureTokenService;
import com.atlassian.bamboo.serialization.WhitelistedSerializable;
import com.atlassian.bamboo.task.RuntimeTaskDataProvider;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.CommonContext;
import com.atlassian.bamboo.v2.build.agent.messages.AuthenticableMessage;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class HubTaskDataProvider implements RuntimeTaskDataProvider {

	private SecureTokenService tokenService;

	@Override
	public Map<String, String> populateRuntimeTaskData(final TaskDefinition taskDef, final CommonContext context) {
		final Map<String, String> map = new HashMap<String, String>();
		final SecureToken token = tokenService
				.generate(AuthenticableMessage.Identification.forResultKey(context.getResultKey()));
		map.put(HubBambooUtils.HUB_TASK_SECURE_TOKEN, token.getToken());

		return map;
	}

	@Override
	public void processRuntimeTaskData(final TaskDefinition taskDef, final CommonContext context) {
		tokenService.invalidate(context.getResultKey());

	}

	@Override
	public Map<String, WhitelistedSerializable> createRuntimeTaskData(final RuntimeTaskDefinition taskDef,
			final CommonContext context) {
		return new HashMap<>();
	}

	public void setSecureTokenService(final SecureTokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	public void processRuntimeTaskData(final RuntimeTaskDefinition taskDef, final CommonContext context) {
		tokenService.invalidate(context.getResultKey());
	}
}
