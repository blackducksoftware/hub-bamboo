package com.blackducksoftware.integration.hub.bamboo.config;

public enum ConfigKeyEnum {

	HUB_URL("hubUrl"), HUB_USER("hubUser"), HUB_PASS("hubPass"), PROXY_URL("hubProxyUrl"), PROXY_PORT(
			"hubProxyPort"), PROXY_NOHOSTS("hubNoProxyHost"), PROXY_USER("hubProxyUser"), PROXY_PASS("hubProxyPass");

	private final String key;
	private final static String keyPrefix = "blackducksoftware.hubConfig.";

	private ConfigKeyEnum(final String key) {
		this.key = key;
	}

	public String getKey() {
		return keyPrefix + key;
	}
}
