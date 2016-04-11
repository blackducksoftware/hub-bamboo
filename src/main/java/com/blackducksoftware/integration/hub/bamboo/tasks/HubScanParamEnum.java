package com.blackducksoftware.integration.hub.bamboo.tasks;

public enum HubScanParamEnum {
	PROJECT("project"), VERSION("version"), PHASE("phase"), DISTRIBUTION("distribution"), GENERATERISKREPORT(
			"generateRiskReport"), MAXWAITTIMEFORRISKREPORT(
					"maxWaitTimeForRiskReport"), SCANMEMORY("scanmemory"), TARGETS("targets");

	private String key;

	private HubScanParamEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
