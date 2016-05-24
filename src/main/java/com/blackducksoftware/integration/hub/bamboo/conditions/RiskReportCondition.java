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
package com.blackducksoftware.integration.hub.bamboo.conditions;

import java.io.File;
import java.util.Map;

import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class RiskReportCondition extends AbstractCompositeCondition {

	private static final String KEY_BUILD_NUMBER = "buildNumber";
	private static final String KEY_PLAN_KEY = "planKey";

	@Override
	public boolean shouldDisplay(final Map<String, Object> buildContextMap) {

		boolean display = false;
		String planKey = "";
		int buildNumber = -1;

		try {
			if (buildContextMap.containsKey(KEY_PLAN_KEY)) {
				planKey = buildContextMap.get(KEY_PLAN_KEY).toString();
			}

			if (buildContextMap.containsKey(KEY_BUILD_NUMBER)) {
				buildNumber = Integer.valueOf(buildContextMap.get(KEY_BUILD_NUMBER).toString()).intValue();
			}
			final File dataFile = HubBambooUtils.getInstance().getRiskReportFile(planKey, buildNumber);

			if (dataFile != null) {
				display = dataFile.exists();
			}
		} catch (final Throwable t) {
			// nothing to do display will be false
		}

		return display;
	}
}
