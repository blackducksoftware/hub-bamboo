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
