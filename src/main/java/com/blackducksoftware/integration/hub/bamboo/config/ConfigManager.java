package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.spring.ComponentAccessor;
import com.atlassian.bandana.BandanaManager;

public class ConfigManager implements Serializable {

	private static final long serialVersionUID = 631572820626880758L;

	private transient Logger logger = Logger.getLogger(ConfigManager.class);

	private EncryptionService encryptionService;
	private transient BandanaManager bandanaManager;

	private final static String HUB_CONFIG_KEY = "com.blackducksoftware.integration.hub.bamboo.configuration";

	// public static ConfigManager getInstance() {
	// final ConfigManager configManager = new ConfigManager();
	// ContainerManager.autowireComponent(configManager);
	// return configManager;
	// }
	//
	// public ConfigManager() {
	//
	// }

	public void setBandanaManager(final BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;

		logger.info("BandanaManager wiring for ConfigManager Before Encryption");
		encryptionService = ComponentAccessor.ENCRYPTION_SERVICE.get();
		logger.info("BandanaManager wiring for ConfigManager After Encryption");
	}

	public HubConfig readConfig() {

		final HubConfig config = (HubConfig) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT,
				HUB_CONFIG_KEY);

		final String hubUser = encryptionService.decrypt(config.getHubUser());
		final String hubProxyUser = encryptionService.decrypt(config.getHubProxyUser());
		return new HubConfig(config.getHubUrl(), hubUser, config.getHubPass(), config.getHubProxyUrl(),
				config.getHubProxyPort(), config.getHubNoProxyHost(), hubProxyUser, config.getHubProxyPass());
	}

	public void writeConfig(final HubConfig config) {

		final String hubUser = encryptionService.encrypt(config.getHubUser());
		final String hubPass = encryptionService.encrypt(config.getHubPass());
		final String hubProxyUser = encryptionService.encrypt(config.getHubProxyUser());
		final String hubProxyPass = encryptionService.encrypt(config.getHubProxyPass());

		final HubConfig encryptedConfig = new HubConfig(config.getHubUrl(), hubUser, hubPass, config.getHubProxyUrl(),
				config.getHubProxyPort(), config.getHubNoProxyHost(), hubProxyUser, hubProxyPass);

		bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, HUB_CONFIG_KEY, encryptedConfig);
	}
}
