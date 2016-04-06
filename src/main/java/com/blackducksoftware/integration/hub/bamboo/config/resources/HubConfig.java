package com.blackducksoftware.integration.hub.bamboo.config.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubConfig {

	@XmlElement
	private String hubUrl;
	@XmlElement
	private String hubUser;
	@XmlElement
	private String hubPass;
	@XmlElement
	private String hubProxyUrl;
	@XmlElement
	private String hubProxyPort;
	@XmlElement
	private String hubNoProxyHost;
	@XmlElement
	private String hubProxyUser;
	@XmlElement
	private String hubProxyPass;

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
