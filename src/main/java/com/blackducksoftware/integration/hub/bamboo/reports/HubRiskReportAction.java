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
package com.blackducksoftware.integration.hub.bamboo.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.utils.process.IOUtils;
import com.blackducksoftware.integration.hub.api.report.HubRiskReportData;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.util.HubResourceBundleHelper;
import com.google.gson.Gson;

public class HubRiskReportAction extends ViewBuildResults {

	private static final long serialVersionUID = 4076165272346339757L;

	private HubRiskReportData hubRiskReportData;
	private HubResourceBundleHelper bundle;

	@Override
	public String doExecute() throws Exception {
		if (hubRiskReportData == null) {
			createReportData();
			bundle = new HubResourceBundleHelper();
			bundle.setKeyPrefix(HubBambooUtils.HUB_I18N_KEY_PREFIX);

		}
		return SUCCESS;
	}

	public HubRiskReportData getHubRiskReportData() {
		return hubRiskReportData;
	}

	public HubResourceBundleHelper getBundle() {
		return bundle;
	}

	private void createReportData() {
		final String planKey = getPlanKey();
		final int buildNumber = getBuildNumber();

		FileReader reader = null;
		try {
			final File fileData = HubBambooUtils.getInstance().getRiskReportFile(planKey, buildNumber);
			reader = new FileReader(fileData);
			final Gson gson = new Gson();
			hubRiskReportData = gson.fromJson(reader, HubRiskReportData.class);

		} catch (final FileNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
}
