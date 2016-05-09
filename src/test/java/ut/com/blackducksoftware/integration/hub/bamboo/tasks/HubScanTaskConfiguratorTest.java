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
