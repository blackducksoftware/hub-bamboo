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
package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.Serializable;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.spring.ComponentAccessor;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.util.concurrent.NotNull;

public class ConfigManager implements Serializable {

	private static final long serialVersionUID = 631572820626880758L;

	private EncryptionService encryptionService;
	private transient BandanaManager bandanaManager;

	private final static String HUB_CONFIG_KEY = "com.blackducksoftware.integration.hub.bamboo.configuration";

	public void setBandanaManager(final BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;
		encryptionService = ComponentAccessor.ENCRYPTION_SERVICE.get();
	}

	public HubConfig readConfig() {

		final HubConfig config = (HubConfig) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT,
				HUB_CONFIG_KEY);

		if (config == null) {
			return new HubConfig();
		} else {

			final String hubUser = encryptionService.decrypt(config.getHubUser());
			final String hubProxyUser = encryptionService.decrypt(config.getHubProxyUser());
			final String hubPass = encryptionService.decrypt(config.getHubPass());
			final String hubProxyPass = encryptionService.decrypt(config.getHubProxyPass());

			return new HubConfig(config.getHubUrl(), hubUser, hubPass, config.getHubProxyUrl(),
					config.getHubProxyPort(), config.getHubNoProxyHost(), hubProxyUser, hubProxyPass);
		}
	}

	public void writeConfig(@NotNull final HubConfig config) {

		final String hubUser = encryptionService.encrypt(config.getHubUser());
		final String hubPass = encryptionService.encrypt(config.getHubPass());
		final String hubProxyUser = encryptionService.encrypt(config.getHubProxyUser());
		final String hubProxyPass = encryptionService.encrypt(config.getHubProxyPass());

		final HubConfig encryptedConfig = new HubConfig(config.getHubUrl(), hubUser, hubPass, config.getHubProxyUrl(),
				config.getHubProxyPort(), config.getHubNoProxyHost(), hubProxyUser, hubProxyPass);

		bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, HUB_CONFIG_KEY, encryptedConfig);
	}
}
