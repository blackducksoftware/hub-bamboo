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
package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bamboo.fileserver.ArtifactStorage;
import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.blackducksoftware.integration.hub.bamboo.BambooFileStorageHelper;

@RunWith(PowerMockRunner.class)
public class BambooFileStorageHelperPowerMockTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private ArtifactDefinitionContext createArtifact() {
		final ArtifactDefinitionContext artifactDefinition = new ArtifactDefinitionContextImpl("Hub_Risk_Report", false,
				null);

		return artifactDefinition;
	}

	private PlanResultKey createResultKey() {
		final PlanResultKey resultKey = PlanKeys.getPlanResultKey("TEST-PLAN-JOB1", 1);
		return resultKey;
	}

	@Test
	public void testEmptyClass() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(BambooFileStorageHelper.ILLEGAL_ARG_MESSAGE_RESULT_KEY);

		final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();
		storageHelper.buildArtifactRootDirectory();
	}

	@Test
	public void testPlanKeyNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(BambooFileStorageHelper.ILLEGAL_ARG_MESSAGE_RESULT_KEY);
		final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();
		storageHelper.setResultKey(null);
		storageHelper.setArtifactDefinition(createArtifact());
		storageHelper.buildArtifactRootDirectory();
	}

	@Test
	public void testArtifactNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(BambooFileStorageHelper.ILLEGAL_ARG_MESSAGE_ARTIFACT_DEFINITION);
		final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();
		storageHelper.setResultKey(createResultKey());
		storageHelper.setArtifactDefinition(null);
		storageHelper.buildArtifactRootDirectory();
	}

	@Test
	public void testGetMethods() throws Exception {
		final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();
		final PlanResultKey key = createResultKey();
		final ArtifactDefinitionContext artifact = createArtifact();
		storageHelper.setResultKey(key);
		storageHelper.setArtifactDefinition(artifact);

		assertEquals(key, storageHelper.getResultKey());
		assertEquals(artifact, storageHelper.getArtifactDefinition());
	}

	@Test
	@PrepareForTest(SystemDirectory.class)
	public void testGetArtifactRootDir() throws Exception {
		final File file = new File("bamboo-artifacts/");
		final ArtifactStorage storage = PowerMockito.mock(ArtifactStorage.class);
		PowerMockito.mockStatic(SystemDirectory.class);
		PowerMockito.when(SystemDirectory.getArtifactStorage()).thenReturn(storage);
		final PlanResultKey key = createResultKey();
		final ArtifactDefinitionContext artifact = createArtifact();
		PowerMockito.when(storage.getArtifactDirectory(key)).thenReturn(file);
		final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();

		storageHelper.setResultKey(key);
		storageHelper.setArtifactDefinition(artifact);

		final File reportFile = storageHelper.buildArtifactRootDirectory();
		final String path = file.getAbsolutePath() + "/Hub_Risk_Report";
		final String reportFilePath = reportFile.getAbsolutePath();
		assertEquals(key, storageHelper.getResultKey());
		assertEquals(artifact, storageHelper.getArtifactDefinition());
		assertEquals(path, reportFilePath);
	}
}
