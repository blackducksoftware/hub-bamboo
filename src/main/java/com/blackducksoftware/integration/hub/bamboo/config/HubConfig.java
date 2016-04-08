package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.Serializable;

public class HubConfig implements Serializable {

	private static final long serialVersionUID = -3260976719663527625L;

	private String hubUrl;
	private String hubUser;
	private String hubPass;
	private String hubProxyUrl;
	private String hubProxyPort;
	private String hubNoProxyHost;
	private String hubProxyUser;
	private String hubProxyPass;

	public HubConfig() {

	}

	public HubConfig(final String hubUrl, final String hubUser, final String hubPass, final String hubProxyUrl,
			final String hubProxyPort, final String hubNoProxyHost, final String hubProxyUser,
			final String hubProxyPass) {

		this.hubUrl = hubUrl;
		this.hubUser = hubUser;
		this.hubPass = hubPass;
		this.hubProxyUrl = hubProxyUrl;
		this.hubProxyPort = hubProxyPort;
		this.hubNoProxyHost = hubNoProxyHost;
		this.hubProxyUser = hubProxyUser;
		this.hubProxyPass = hubProxyPass;
	}

	public String getHubUrl() {
		return hubUrl;
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = hubUrl;
	}

	public String getHubUser() {
		return hubUser;
	}

	public void setHubUser(final String hubUser) {
		this.hubUser = hubUser;
	}

	public String getHubPass() {
		return hubPass;
	}

	public void setHubPass(final String hubPass) {
		this.hubPass = hubPass;
	}

	public String getHubProxyUrl() {
		return hubProxyUrl;
	}

	public void setHubProxyUrl(final String hubProxyUrl) {
		this.hubProxyUrl = hubProxyUrl;
	}

	public String getHubProxyPort() {
		return hubProxyPort;
	}

	public void setHubProxyPort(final String hubProxyPort) {
		this.hubProxyPort = hubProxyPort;
	}

	public String getHubNoProxyHost() {
		return hubNoProxyHost;
	}

	public void setHubNoProxyHost(final String hubNoProxyHost) {
		this.hubNoProxyHost = hubNoProxyHost;
	}

	public String getHubProxyUser() {
		return hubProxyUser;
	}

	public void setHubProxyUser(final String hubProxyUser) {
		this.hubProxyUser = hubProxyUser;
	}

	public String getHubProxyPass() {
		return hubProxyPass;
	}

	public void setHubProxyPass(final String hubProxyPass) {
		this.hubProxyPass = hubProxyPass;
	}
}
