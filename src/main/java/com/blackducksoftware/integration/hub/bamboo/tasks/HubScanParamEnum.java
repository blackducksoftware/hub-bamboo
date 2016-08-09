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
package com.blackducksoftware.integration.hub.bamboo.tasks;

import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;

public enum HubScanParamEnum {
	PROJECT("hubProject", ""),
	VERSION("hubVersion", ""),
	PHASE("hubPhase", PhaseEnum.PLANNING.getDisplayValue()),
	DISTRIBUTION("hubDistribution", DistributionEnum.EXTERNAL.getDisplayValue()),
	GENERATE_RISK_REPORT("generateRiskReport", "false"),
	MAX_WAIT_TIME_FOR_BOM_UPDATE("maxWaitTimeForBomUpdate", "5"),
	SCANMEMORY("hubScanMemory", "4096"),
	TARGETS("hubTargets", ""),
	FAIL_ON_POLICY_VIOLATION("failOnPolicyViolation", "false");

	private String key;
	private String defaultValue;

	private HubScanParamEnum(final String key, final String defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
