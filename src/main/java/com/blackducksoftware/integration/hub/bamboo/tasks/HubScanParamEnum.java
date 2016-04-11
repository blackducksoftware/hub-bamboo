package com.blackducksoftware.integration.hub.bamboo.tasks;

public enum HubScanParamEnum {
	PROJECT("hubProject"), VERSION("hubVersion"), PHASE("hubPhase"), DISTRIBUTION(
			"hubDistribution"), GENERATERISKREPORT("generateRiskReport"), MAXWAITTIMEFORRISKREPORT(
					"maxWaitTimeForRiskReport"), SCANMEMORY("hubScanMemory"), TARGETS("hubTargets");

	private String key;

	private HubScanParamEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
