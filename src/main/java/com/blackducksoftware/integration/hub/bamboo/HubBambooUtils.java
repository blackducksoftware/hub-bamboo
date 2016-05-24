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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.bamboo.fileserver.ArtifactStorage;
import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.utils.SystemProperty;
import com.atlassian.util.concurrent.NotNull;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.builder.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;

public class HubBambooUtils implements Cloneable {

	private final static HubBambooUtils instance = new HubBambooUtils();
	public final static String HUB_RISK_REPORT_FILENAME = "hub_risk_report.json";
	public final static String HUB_TASK_SECURE_TOKEN = "hub_task_secure_token";
	public final static String HUB_RISK_REPORT_ARTIFACT_NAME = "Hub_Risk_Report";
	public final static String HUB_I18N_KEY_PREFIX = "hub.riskreport";

	public static HubBambooUtils getInstance() {
		return instance;
	}

	private HubBambooUtils() {

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public HubServerConfig buildConfigFromStrings(final String hubUrl, final String hubUser, final String hubPass,
			final String hubPassLength, final String hubProxyUrl, final String hubProxyPort,
			final String hubProxyNoHost, final String hubProxyUser, final String hubProxyPass,
			final String hubProxyPassLength) {
		final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder(true);
		configBuilder.setHubUrl(hubUrl);
		configBuilder.setUsername(hubUser);
		configBuilder.setPassword(hubPass);
		configBuilder.setProxyHost(hubProxyUrl);
		configBuilder.setProxyPort(hubProxyPort);
		configBuilder.setProxyUsername(hubProxyUser);
		configBuilder.setProxyPassword(hubProxyPass);

		configBuilder.setIgnoredProxyHosts(hubProxyNoHost);
		int length = 0;
		if (StringUtils.isNotBlank(hubPassLength)) {
			length = Integer.valueOf(hubPassLength);
			configBuilder.setPasswordLength(length);
		}

		if (StringUtils.isNotBlank(hubProxyPassLength)) {
			length = Integer.valueOf(hubProxyPassLength);
			configBuilder.setProxyPasswordLength(length);
		}

		return configBuilder.build().getConstructedObject();
	}

	public HubProxyInfo buildProxyInfoFromString(final String hubProxyUrl, final String hubProxyPort,
			final String hubProxyNoHost, final String hubProxyUser, final String hubProxyPass)
			throws IllegalArgumentException, EncryptionException, HubIntegrationException {
		HubProxyInfo proxyInfo = null;
		if (StringUtils.isNotBlank(hubProxyUrl)) {
			final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
			proxyBuilder.setHost(hubProxyUrl);
			if (StringUtils.isNotBlank(hubProxyPort)) {
				try {
					proxyBuilder.setPort(Integer.valueOf(hubProxyPort));
				} catch (final NumberFormatException ex) {
					// ignore the default value is 0.
				}

				proxyBuilder.setIgnoredProxyHosts(hubProxyNoHost);
				proxyBuilder.setUsername(hubProxyUser);
				proxyBuilder.setPassword(hubProxyPass);
				proxyInfo = proxyBuilder.build().getConstructedObject();
			}
		}
		return proxyInfo;
	}

	public void configureProxyToService(final HubServerConfig hubConfig, final HubIntRestService service) {

		final HubProxyInfo proxyInfo = hubConfig.getProxyInfo();

		if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
			if (proxyInfo.shouldUseProxyForUrl(hubConfig.getHubUrl())) {
				service.setProxyProperties(proxyInfo);
			}
		}
	}

	public List<String> createScanTargetPaths(final String targetPathText, final File workingDirectory) {

		final List<String> scanTargets = new ArrayList<String>();

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

	public Map<String, String> getEnvironmentVariablesMap(@NotNull final Map<String, String> systemVariables,
			@NotNull final Map<String, String> taskContextVariables) {
		final Map<String, String> allVariablesMap = new HashMap<String, String>(
				systemVariables.size() + taskContextVariables.size());

		allVariablesMap.putAll(systemVariables);
		allVariablesMap.putAll(taskContextVariables);

		return allVariablesMap;
	}

	public String getEnvironmentVariable(@NotNull final Map<String, String> envVars,
			@NotNull final String parameterName, final boolean taskContextVariable) {
		String variable;

		if (taskContextVariable) {
			variable = "bamboo_" + parameterName;
		} else {
			variable = parameterName;
		}

		final String value = envVars.get(variable);

		return StringUtils.trimToNull(value);
	}

	public String getBambooHome() {
		try {
			final File bambooHome = SystemDirectory.getApplicationHome();
			return bambooHome.getAbsolutePath();
		} catch (final NullPointerException npe) {
			return SystemProperty.BAMBOO_HOME_FROM_ENV.getValue();
		}
	}

	public File getRiskReportFile(final String planKey, final int buildNumber) {
		final PlanResultKey resultKey = PlanKeys.getPlanResultKey(planKey, buildNumber);
		// TODO Have to test this on another server SystemDirectory may
		// fail on remote Bamboo server.
		final ArtifactStorage storage = SystemDirectory.getArtifactStorage();

		final File planRoot = storage.getArtifactDirectory(resultKey);
		final File riskReportRoot = new File(planRoot, HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME);
		final File dataFile = new File(riskReportRoot, HubBambooUtils.HUB_RISK_REPORT_FILENAME);
		return dataFile;
	}
}
