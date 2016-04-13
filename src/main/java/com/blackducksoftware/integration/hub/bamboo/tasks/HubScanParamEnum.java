package com.blackducksoftware.integration.hub.bamboo.tasks;

public enum HubScanParamEnum {
	PROJECT("hubProject"), VERSION("hubVersion"), PHASE("hubPhase"), DISTRIBUTION(
			"hubDistribution"), GENERATE_RISK_REPORT("generateRiskReport"), MAX_WAIT_TIME_FOR_BOM_UPDATE(
					"maxWaitTimeForBomUpdate"), SCANMEMORY("hubScanMemory"), TARGETS("hubTargets");

	private String key;

	private HubScanParamEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
