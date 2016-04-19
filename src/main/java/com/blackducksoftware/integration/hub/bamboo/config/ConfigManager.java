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

	private final static String HUB_CONFIG_KEY_PREFIX = "com.blackducksoftware.integration.hub.bamboo.configuration";
	private final static String CONFIG_HUB_URL = ".huburl";
	private final static String CONFIG_HUB_USER = ".hubuser";
	private final static String CONFIG_HUB_PASS = ".hubpassword";
	private final static String CONFIG_PROXY_URL = ".hubproxyurl";
	private final static String CONFIG_PROXY_PORT = ".hubproxyport";
	private final static String CONFIG_PROXY_USER = ".hubproxyuser";
	private final static String CONFIG_PROXY_PASS = ".hubproxypass";
	private final static String CONFIG_PROXY_NO_HOST = ".hubproxynohost";

	public void setBandanaManager(final BandanaManager bandanaManager) {
		this.bandanaManager = bandanaManager;
		encryptionService = ComponentAccessor.ENCRYPTION_SERVICE.get();
	}

	public HubConfig readConfig() {

		final String hubUrl = getPersistedValue(CONFIG_HUB_URL, false);
		final String hubUser = getPersistedValue(CONFIG_HUB_USER, true);
		final String hubPass = getPersistedValue(CONFIG_HUB_PASS, true);
		final String hubProxyUrl = getPersistedValue(CONFIG_PROXY_URL, false);
		final String hubProxyPort = getPersistedValue(CONFIG_PROXY_PORT, false);
		final String hubProxyNoHost = getPersistedValue(CONFIG_PROXY_NO_HOST, false);
		final String hubProxyUser = getPersistedValue(CONFIG_PROXY_USER, true);
		final String hubProxyPass = getPersistedValue(CONFIG_PROXY_PASS, true);

		return new HubConfig(hubUrl, hubUser, hubPass, hubProxyUrl, hubProxyPort, hubProxyNoHost, hubProxyUser,
				hubProxyPass);
	}

	public void writeConfig(@NotNull final HubConfig config) {

		persistValue(CONFIG_HUB_URL, config.getHubUrl(), false);
		persistValue(CONFIG_HUB_USER, config.getHubUser(), true);
		persistValue(CONFIG_HUB_PASS, config.getHubPass(), true);
		persistValue(CONFIG_PROXY_URL, config.getHubProxyUrl(), false);
		persistValue(CONFIG_PROXY_PORT, config.getHubProxyPort(), false);
		persistValue(CONFIG_PROXY_NO_HOST, config.getHubNoProxyHost(), false);
		persistValue(CONFIG_PROXY_USER, config.getHubProxyUser(), true);
		persistValue(CONFIG_PROXY_PASS, config.getHubProxyPass(), true);

	}

	private String getPersistedValue(final String key, final boolean decrypt) {

		String value = null;

		value = (String) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, HUB_CONFIG_KEY_PREFIX + key);

		if (decrypt) {
			final String original = value;

			value = encryptionService.decrypt(original);
		}

		return value;
	}

	private void persistValue(final String key, final String value, final boolean encrypt) {

		String persist = value;

		if (encrypt) {
			persist = encryptionService.encrypt(value);
		}

		bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, HUB_CONFIG_KEY_PREFIX + key, persist);

	}
}
