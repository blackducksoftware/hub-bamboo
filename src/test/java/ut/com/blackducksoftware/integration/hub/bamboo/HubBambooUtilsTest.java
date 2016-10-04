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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.security.SecureToken;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubBambooUtilsTest {

	private static final String PASSWORD = "password";
	private static final String USER = "user";
	private static final String HUB_URL = "https://google.com";
	private static final String VALID_PORT = "2303";
	private static final String VALID_HOST = "http://yahoo.com";
	private static final String VALID_PASSWORD = "itsasecret";
	private static final String VALID_USERNAME = "memyselfandi";
	private static final String VALID_IGNORE_HOST_LIST = "google,msn,yahoo";
	private static final String VALID_IGNORE_HOST = "google";
	private static final String INVALID_IGNORE_HOST_LIST = "google,[^-z!,abc";
	private static final String INVALID_IGNORE_HOST = "[^-z!";
	private static final String EMPTY_PASSWORD_LENGTH = "";

	private final Map<String, String> map1 = new HashMap<>();
	private final Map<String, String> map2 = new HashMap<>();

	private static final String key_1 = "key1";
	private static final String key_2 = "key2";
	private static final String key_3 = "key3";
	private static final String key_4 = "key4";
	private static final String value_1 = "value1";
	private static final String value_2 = "value2";
	private static final String value_3 = "value3";
	private static final String value_4 = "value4";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private Map<String, String> createVarMap() {
		map1.put(key_1, value_1);
		map1.put(key_2, value_2);
		map2.put(key_3, value_3);
		map2.put(key_4, value_4);

		final Map<String, String> result = HubBambooUtils.getInstance().getEnvironmentVariablesMap(map1, map2);
		return result;
	}

	@Test
	public void testGetInstance() throws Exception {
		assertNotNull(HubBambooUtils.getInstance());
	}

	@Test
	public void testBuildConfig() throws Exception {
		final ValidationResults<GlobalFieldKey, HubServerConfig> results = HubBambooUtils.getInstance()
				.buildConfigFromStrings(HUB_URL, USER, PASSWORD, EMPTY_PASSWORD_LENGTH, VALID_HOST, VALID_PORT,
						VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, EMPTY_PASSWORD_LENGTH);
		final HubServerConfig config = results.getConstructedObject();
		assertNotNull(config);
		assertEquals(new URL(HUB_URL), config.getHubUrl());
		assertEquals(USER, config.getGlobalCredentials().getUsername());
		assertEquals(PASSWORD, config.getGlobalCredentials().getDecryptedPassword());
	}


	@Test
	public void testInvalidConfig() throws Exception {
		HubBambooUtils.getInstance().buildConfigFromStrings(null, null, null, null, VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, EMPTY_PASSWORD_LENGTH);
	}

	@Test
	public void testValidScanTargetList() throws Exception {
		final String targetText = "aFile";
		final File workingDir = new File(".");
		List<String> targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, targetText).getAbsolutePath(), targets.get(0));

		targets = HubBambooUtils.getInstance().createScanTargetPaths("", workingDir);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());

		targets = HubBambooUtils.getInstance().createScanTargetPaths(null, workingDir);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());
	}

	@Test
	public void testValidScanTargetPath() throws Exception {
		String targetText = "aFile\r\nanotherFile";
		final File workingDir = new File(".");
		List<String> targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(2, targets.size());

		targetText = "aFile\nanotherFile";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(2, targets.size());
		assertEquals(new File(workingDir, "aFile").getAbsolutePath(), targets.get(0));
		assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(), targets.get(1));

		targetText = " \nanotherFile";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(), targets.get(0));

		targetText = "aFile\n ";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, "aFile").getAbsolutePath(), targets.get(0));
	}

	@Test
	public void testConfigureServiceNullProxy() throws Exception {
		final RestConnection restConnection = new RestConnection(HUB_URL);
		final ValidationResults<GlobalFieldKey, HubServerConfig> results = HubBambooUtils.getInstance()
				.buildConfigFromStrings(HUB_URL, USER, PASSWORD, EMPTY_PASSWORD_LENGTH, null, null, null, null, null,
						null);
		final HubServerConfig config = results.getConstructedObject();
		HubBambooUtils.getInstance().configureProxyToService(config, restConnection);
	}


	@Test
	public void testCreateEnvVarMap() {

		final Map<String, String> result = createVarMap();
		assertEquals(result.size(), map1.size() + map2.size());
		assertEquals(result.get(key_1), value_1);
		assertEquals(result.get(key_2), value_2);
		assertEquals(result.get(key_3), value_3);
		assertEquals(result.get(key_4), value_4);
		assertFalse(result.containsKey("anunknownkey"));
	}


	@Test
	public void testGetRiskReportArtifactDefinition() {
		final SecureToken token = SecureToken.createFromString("01234546789");
		final ArtifactDefinitionContext definition = HubBambooUtils.getInstance()
				.getRiskReportArtifactDefinitionContext(token);

		assertEquals(HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME, definition.getName());
		assertEquals(HubBambooUtils.HUB_RISK_REPORT_FILENAME, definition.getCopyPattern());
		assertFalse(definition.isSharedArtifact());
		assertEquals(token, definition.getSecureToken());
	}
}
