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
package com.blackducksoftware.integration.hub.bamboo;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContext;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionContextImpl;
import com.atlassian.bamboo.security.SecureToken;
import com.atlassian.bamboo.utils.SystemProperty;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;

public class HubBambooUtils implements Cloneable {

    private static HubBambooUtils myInstance = null;

    public final static String HUB_RISK_REPORT_FILENAME = "riskreport.html";

    public final static String HUB_TASK_SECURE_TOKEN = "hub_task_secure_token";

    public final static String HUB_RISK_REPORT_ARTIFACT_NAME = "Hub_Risk_Report";

    public final static String HUB_I18N_KEY_PREFIX = "hub.riskreport";

    public static HubBambooUtils getInstance() {

        if (myInstance == null) {
            myInstance = new HubBambooUtils();
        }
        return myInstance;
    }

    private HubBambooUtils() {

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public ValidationResults<GlobalFieldKey, HubServerConfig> buildConfigFromStrings(final String hubUrl,
            final String hubUser, final String hubPass, final String hubPassLength, final String hubProxyUrl,
            final String hubProxyPort, final String hubProxyNoHost, final String hubProxyUser,
            final String hubProxyPass, final String hubProxyPassLength) {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder(true);
        configBuilder.setHubUrl(hubUrl);
        configBuilder.setUsername(hubUser);
        configBuilder.setPassword(hubPass);
        configBuilder.setProxyHost(hubProxyUrl);
        configBuilder.setProxyPort(hubProxyPort);
        configBuilder.setProxyUsername(hubProxyUser);

        configBuilder.setIgnoredProxyHosts(hubProxyNoHost);
        int length = 0;
        if (StringUtils.isNotBlank(hubPassLength)) {
            length = Integer.valueOf(hubPassLength);
            configBuilder.setPasswordLength(length);
        }

        if (StringUtils.isNotBlank(hubProxyUser)) {
            configBuilder.setProxyPassword(hubProxyPass);
            if (StringUtils.isNotBlank(hubProxyPassLength)) {
                length = Integer.valueOf(hubProxyPassLength);
                configBuilder.setProxyPasswordLength(length);
            }
        }
        return configBuilder.buildResults();
    }

    public List<String> createScanTargetPaths(final String targetPathText, final File workingDirectory) {

        final List<String> scanTargets = new ArrayList<>();

        if (StringUtils.isNotBlank(targetPathText)) {
            final String[] scanTargetPathsArray = targetPathText.split("\\r?\\n");
            for (final String target : scanTargetPathsArray) {
                if (!StringUtils.isBlank(target)) {
                    if (workingDirectory != null && StringUtils.isBlank(workingDirectory.getAbsolutePath())) {
                        scanTargets.add(target);

                    } else {
                        scanTargets.add(new File(workingDirectory, target).getAbsolutePath());
                    }
                }
            }
        }

        return scanTargets;
    }

    public Map<String, String> getEnvironmentVariablesMap(final Map<String, String> systemVariables,
            final Map<String, String> taskContextVariables) {
        final Map<String, String> allVariablesMap = new HashMap<>(
                systemVariables.size() + taskContextVariables.size());

        trimBambooEnvironmentVariables(allVariablesMap, systemVariables);
        trimBambooEnvironmentVariables(allVariablesMap, taskContextVariables);
        return allVariablesMap;
    }

    private void trimBambooEnvironmentVariables(final Map<String, String> newEnvMap,
            final Map<String, String> envVars) {
        for (final Entry<String, String> entry : envVars.entrySet()) {
            String key = entry.getKey();
            final String value = entry.getValue();
            if (key.startsWith("bamboo_")) {
                key = key.replace("bamboo_", "");
            }
            newEnvMap.put(key, value);
        }
    }

    public String getBambooHome() {

        File bambooHome = null;
        // On remote agents SystemDirectory.getApplicationHome may throw a NPE,
        // because it calls another get method in SystemDirectory. On the master
        // node the call works. When remote agents start up
        // the Bamboo home environment variable is set. This code is needed
        // because we observed different behavior on the master and remote
        // nodes.
        try {
            bambooHome = SystemDirectory.getApplicationHome();
        } catch (final NullPointerException npe) {
        }

        if (bambooHome != null) {
            return bambooHome.getAbsolutePath();
        } else {
            return SystemProperty.BAMBOO_HOME_FROM_ENV.getValue();
        }
    }

    public File getRiskReportFile(final String planKey, final int buildNumber) throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final PlanResultKey resultKey = PlanKeys.getPlanResultKey(planKey, buildNumber);
        final ArtifactDefinitionContext artifact = getRiskReportArtifactDefinitionContext(null);
        final BambooFileStorageHelper storageHelper = new BambooFileStorageHelper();
        storageHelper.setResultKey(resultKey);
        storageHelper.setArtifactDefinition(artifact);
        final File planRoot = storageHelper.buildArtifactRootDirectory();
        final File dataFile = new File(planRoot, HubBambooUtils.HUB_RISK_REPORT_FILENAME);
        return dataFile;
    }

    public ArtifactDefinitionContext getRiskReportArtifactDefinitionContext(final SecureToken token) {
        final ArtifactDefinitionContextImpl artifact = new ArtifactDefinitionContextImpl(
                HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME, false, token);
        artifact.setCopyPattern(HubBambooUtils.HUB_RISK_REPORT_FILENAME);
        return artifact;
    }
}
