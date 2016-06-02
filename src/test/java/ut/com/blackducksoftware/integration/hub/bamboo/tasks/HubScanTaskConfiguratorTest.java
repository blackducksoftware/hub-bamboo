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
package ut.com.blackducksoftware.integration.hub.bamboo.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.bamboo.tasks.HubScanParamEnum;
import com.blackducksoftware.integration.hub.bamboo.tasks.HubScanTaskConfigurator;

import ut.com.blackducksoftware.integration.hub.bamboo.utils.TestTaskDefinition;

public class HubScanTaskConfiguratorTest {

	private Map<String, String> createConfigurationMap() {
		final Map<String, String> configMap = new HashMap<String, String>();
		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			configMap.put(key, param.getDefaultValue());
		}
		return configMap;
	}

	@Test
	public void testPopulateContextForCreate() {
		final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();

		final Map<String, Object> context = new HashMap<String, Object>();
		taskConfigurator.populateContextForCreate(context);

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			assertEquals(param.getDefaultValue(), context.get(key));
		}
	}

	@Test
	public void testPopulateContextForEdit() {
		final TestTaskDefinition taskDefinition = new TestTaskDefinition();
		taskDefinition.setConfiguration(createConfigurationMap());
		final Map<String, Object> context = new HashMap<String, Object>();

		final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();
		taskConfigurator.populateContextForEdit(context, taskDefinition);

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			assertEquals(param.getDefaultValue(), context.get(key));
		}
	}

	@Test
	public void testPopulateContextForView() {
		final TestTaskDefinition taskDefinition = new TestTaskDefinition();
		taskDefinition.setConfiguration(createConfigurationMap());
		final Map<String, Object> context = new HashMap<String, Object>();

		final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();
		taskConfigurator.populateContextForView(context, taskDefinition);

		for (final HubScanParamEnum param : HubScanParamEnum.values()) {
			final String key = param.getKey();
			assertEquals(param.getDefaultValue(), context.get(key));
		}
	}
}
