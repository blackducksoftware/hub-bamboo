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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.security.SecureToken;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class HubBambooUtilsTest {

    private static final String PASSWORD = "password";

    private static final String USER = "user";

    private static final String HUB_URL = "https://www.google.com";

    private static final String VALID_PORT = "3128";

    private static final String VALID_HOST = "tank.blackducksoftware.com";

    private static final String VALID_PASSWORD = "itsasecret";

    private static final String VALID_USERNAME = "memyselfandi";

    private static final String VALID_IGNORE_HOST = ".*google.com.*";

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
    public void testInvalidConfig() throws Exception {
        exception.expect(IllegalStateException.class);
        HubBambooUtils.getInstance().buildConfigFromStrings(null, null, null, null, null, VALID_HOST, VALID_PORT,
                VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, EMPTY_PASSWORD_LENGTH);
    }

    @Test
    public void testValidScanTargetList() throws Exception {
        final String targetText = "aFile";
        final File workingDir = new File(".");
        List<String> targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
        final String expectedPath = new File(workingDir, targetText).getCanonicalPath();
        final String actualPath = targets.get(0);
        assertNotNull(targets);
        assertEquals(1, targets.size());
        assertEquals(expectedPath, actualPath);

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
        assertEquals(new File(workingDir, "aFile").getCanonicalPath(), targets.get(0));
        assertEquals(new File(workingDir, "anotherFile").getCanonicalPath(), targets.get(1));

        targetText = " \nanotherFile";
        targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
        assertNotNull(targets);
        assertEquals(1, targets.size());
        assertEquals(new File(workingDir, "anotherFile").getCanonicalPath(), targets.get(0));

        targetText = "aFile\n ";
        targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
        assertNotNull(targets);
        assertEquals(1, targets.size());
        assertEquals(new File(workingDir, "aFile").getCanonicalPath(), targets.get(0));
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
