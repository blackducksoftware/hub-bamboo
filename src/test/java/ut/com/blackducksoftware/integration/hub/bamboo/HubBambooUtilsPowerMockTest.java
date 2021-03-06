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
package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bamboo.fileserver.ArtifactStorage;
import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.utils.SystemProperty;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

@RunWith(PowerMockRunner.class)
public class HubBambooUtilsPowerMockTest {

    @PrepareForTest(SystemDirectory.class)
    @Test
    public void testGetBambooHome() throws Exception {
        final File file = new File("bamboo-home/");
        PowerMockito.mockStatic(SystemDirectory.class);
        PowerMockito.when(SystemDirectory.getApplicationHome()).thenReturn(file);

        final String path = HubBambooUtils.getInstance().getBambooHome();

        assertEquals(file.getCanonicalPath(), path);

        PowerMockito.verifyStatic();
        SystemDirectory.getApplicationHome();
    }

    @Test
    public void testGetBambooHomeFromEnv() throws Exception {
        final File file = new File("bamboo-home/");
        SystemProperty.BAMBOO_HOME_FROM_ENV.setValue(file.getCanonicalPath());
        final String path = HubBambooUtils.getInstance().getBambooHome();

        assertEquals(file.getCanonicalPath(), path);
        assertEquals(file.getCanonicalPath(), SystemProperty.BAMBOO_HOME_FROM_ENV.getValue());
    }

    @PrepareForTest(SystemDirectory.class)
    @Test
    public void testGetRiskReport() throws Exception {
        final File file = new File("bamboo-artifacts/");
        final ArtifactStorage storage = PowerMockito.mock(ArtifactStorage.class);
        PowerMockito.mockStatic(SystemDirectory.class);
        PowerMockito.when(SystemDirectory.getArtifactStorage()).thenReturn(storage);
        PowerMockito.when(storage.getArtifactDirectory(Mockito.any(PlanResultKey.class))).thenReturn(file);

        final File reportFile = HubBambooUtils.getInstance().getRiskReportFile("TEST-PLAN-JOB1", 1);
        final String path = file.getCanonicalPath() + "/Hub_Risk_Report/riskreport.html";
        final String reportFilePath = reportFile.getCanonicalPath();
        assertEquals(path, reportFilePath);
    }
}
