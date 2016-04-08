package com.blackducksoftware.integration.hub.bamboo.config.actions;

import org.apache.log4j.Logger;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;

public class ConfigHubServerAction extends BambooActionSupport implements GlobalAdminSecurityAware {

	private static final long serialVersionUID = 4380000697000607530L;

	private transient Logger logger = Logger.getLogger(ConfigHubServerAction.class);

	private String hubUrl;
	private String hubUser;
	private String hubPass;
	private String hubProxyUrl;
	private String hubProxyPort;
	private String hubNoProxyHost;
	private String hubProxyUser;
	private String hubProxyPass;

	private transient ConfigManager configManager;

	// public ConfigHubServerAction() {
	// final ConfigManager configManager =
	// ContainerManager.getComponent("hubServerConfigManager",
	// ConfigManager.class);
	// final HubConfig config = configManager.readConfig();
	// updateLocalMembers(config);
	// }

	private void updateLocalMembers(final HubConfig config) {
		setHubUrl(config.getHubUrl());
		setHubUser(config.getHubUser());
		setHubPass(config.getHubPass());

		setHubProxyUrl(config.getHubProxyUrl());
		setHubProxyPort(config.getHubProxyPort());
		setHubNoProxyHost(config.getHubNoProxyHost());
		setHubProxyUser(config.getHubProxyUser());
		setHubProxyPass(config.getHubProxyPass());
	}

	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;

		if (this.configManager != null) {
			// final HubConfig config = configManager.readConfig();
			// updateLocalMembers(config);
		}
	}

	@Override
	public void validate() {

		// use hub ci common validation here....
		super.validate();
	}

	public String doEditConfig() {

		final HubConfig config = new HubConfig(getHubUrl(), getHubUser(), getHubPass(), getHubProxyUrl(),
				getHubProxyPort(), getHubNoProxyHost(), getHubProxyUser(), getHubProxyPass());
		configManager.writeConfig(config);

		return SUCCESS;
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
