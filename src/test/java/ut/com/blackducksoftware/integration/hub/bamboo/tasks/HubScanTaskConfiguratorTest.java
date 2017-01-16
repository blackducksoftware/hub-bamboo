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
package ut.com.blackducksoftware.integration.hub.bamboo.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.bamboo.tasks.HubScanTaskConfigurator;
import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.hub.validator.HubScanConfigValidator;

import ut.com.blackducksoftware.integration.hub.bamboo.utils.TestTaskDefinition;

public class HubScanTaskConfiguratorTest {

    private Map<String, String> createConfigurationMap() {
        final Map<String, String> configMap = new HashMap<>();
        configMap.put(HubScanConfigFieldEnum.PROJECT.getKey(), "");
        configMap.put(HubScanConfigFieldEnum.VERSION.getKey(), "");
        configMap.put(HubScanConfigFieldEnum.PHASE.getKey(), PhaseEnum.PLANNING.getDisplayValue());
        configMap.put(HubScanConfigFieldEnum.DISTRIBUTION.getKey(), DistributionEnum.EXTERNAL.getDisplayValue());
        configMap.put(HubScanConfigFieldEnum.GENERATE_RISK_REPORT.getKey(), "false");
        configMap.put(HubScanConfigFieldEnum.DRY_RUN.getKey(), "false");
        configMap.put(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey(),
                String.valueOf(HubScanConfigValidator.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES));
        configMap.put(HubScanConfigFieldEnum.SCANMEMORY.getKey(), String.valueOf(HubScanConfigValidator.DEFAULT_MEMORY_IN_MEGABYTES));
        configMap.put(HubScanConfigFieldEnum.TARGETS.getKey(), "");
        configMap.put(HubScanConfigFieldEnum.FAIL_ON_POLICY_VIOLATION.getKey(), "false");
        return configMap;
    }

    private void assertDefaultValues(final Map<String, Object> context) {
        assertEquals("", context.get(HubScanConfigFieldEnum.PROJECT.getKey()));
        assertEquals("", context.get(HubScanConfigFieldEnum.VERSION.getKey()));
        assertEquals(PhaseEnum.PLANNING.getDisplayValue(), context.get(HubScanConfigFieldEnum.PHASE.getKey()));
        assertEquals(DistributionEnum.EXTERNAL.getDisplayValue(), context.get(HubScanConfigFieldEnum.DISTRIBUTION.getKey()));
        assertEquals("false", context.get(HubScanConfigFieldEnum.GENERATE_RISK_REPORT.getKey()));
        assertEquals("false", context.get(HubScanConfigFieldEnum.DRY_RUN.getKey()));
        assertEquals(String.valueOf(HubScanConfigValidator.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES),
                context.get(HubScanConfigFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE.getKey()));
        assertEquals(String.valueOf(HubScanConfigValidator.DEFAULT_MEMORY_IN_MEGABYTES), context.get(HubScanConfigFieldEnum.SCANMEMORY.getKey()));
        assertEquals("", context.get(HubScanConfigFieldEnum.TARGETS.getKey()));
        assertEquals("false", context.get(HubScanConfigFieldEnum.FAIL_ON_POLICY_VIOLATION.getKey()));
    }

    @Test
    public void testPopulateContextForCreate() {
        final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();

        final Map<String, Object> context = new HashMap<>();
        taskConfigurator.populateContextForCreate(context);

        assertDefaultValues(context);
    }

    @Test
    public void testPopulateContextForEdit() {
        final TestTaskDefinition taskDefinition = new TestTaskDefinition();
        taskDefinition.setConfiguration(createConfigurationMap());
        final Map<String, Object> context = new HashMap<>();

        final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();
        taskConfigurator.populateContextForEdit(context, taskDefinition);

        assertDefaultValues(context);
    }

    @Test
    public void testPopulateContextForView() {
        final TestTaskDefinition taskDefinition = new TestTaskDefinition();
        taskDefinition.setConfiguration(createConfigurationMap());
        final Map<String, Object> context = new HashMap<>();

        final HubScanTaskConfigurator taskConfigurator = new HubScanTaskConfigurator();
        taskConfigurator.populateContextForView(context, taskDefinition);

        assertDefaultValues(context);
    }

}
