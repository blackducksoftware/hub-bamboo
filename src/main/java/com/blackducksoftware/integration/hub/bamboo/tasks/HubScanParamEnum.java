/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.bamboo.tasks;

import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

public enum HubScanParamEnum {
	PROJECT("hubProject", ""),
	VERSION("hubVersion", ""),
	PHASE("hubPhase", PhaseEnum.PLANNING.getDisplayValue()),
	DISTRIBUTION("hubDistribution", DistributionEnum.EXTERNAL.getDisplayValue()),
	GENERATE_RISK_REPORT("generateRiskReport", "false"),
	MAX_WAIT_TIME_FOR_BOM_UPDATE("maxWaitTimeForBomUpdate", "5"),
	SCANMEMORY("hubScanMemory", "4096"),
	TARGETS("hubTargets", ""),
	FAIL_ON_POLICY_VIOLATION("failOnPolicyViolation", "false"),
	HUB_URL("hubUrl", ""),
	HUB_USER("hubUser", ""),
	HUB_PASS("hubPass", ""),
	HUB_TIMEOUT("hubTimeout", ""),
	PROXY_HOST("hubProxyHost", ""),
	PROXY_PORT("hubProxyPort", ""),
	PROXY_USER("hubProxyUser", ""),
	PROXY_PASS("hubProxyPass", ""),
	PROXY_NO_HOST("hubProxyNoHost", "");

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
