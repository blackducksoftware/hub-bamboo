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
package com.blackducksoftware.integration.hub.bamboo.config.actions;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResult;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.logging.IntBufferedLogger;

public class ConfigHubServerAction extends BambooActionSupport implements GlobalAdminSecurityAware {

	private static final long serialVersionUID = -6414177581553775092L;

	private static final String READING_PERSISTED_CONFIG_WARNING = "Reading persisted config warning";
	private static final String TEST_CONNECTION_FAILED = "Test Connection Failed";

	private final Logger logger = Logger.getLogger(ConfigHubServerAction.class);

	private final static String HUB_CONFIG_MODE_SUBMIT = "submit";
	private final static String HUB_CONFIG_MODE_TEST = "test";

	private String hubUrl;
	private String hubUser;
	private String hubPass;
	private String hubProxyUrl;
	private String hubProxyPort;
	private String hubNoProxyHost;
	private String hubProxyUser;
	private String hubProxyPass;
	private String hubConfigMode;

	private ConfigManager configManager;

	private void updateLocalMembers(final HubServerConfig config) throws IllegalArgumentException, EncryptionException {
		if (config.getHubUrl() != null) {
			setHubUrl(config.getHubUrl().toString());
		}

		if (config.getGlobalCredentials() != null) {
			setHubUser(config.getGlobalCredentials().getUsername());
			setHubPass(config.getGlobalCredentials().getDecryptedPassword());
		}

		if (config.getProxyInfo() != null) {
			setHubProxyUrl(config.getProxyInfo().getHost());
			setHubProxyPort(String.valueOf(config.getProxyInfo().getPort()));
			setHubNoProxyHost(config.getProxyInfo().getIgnoredProxyHosts());
			setHubProxyUser(config.getProxyInfo().getUsername());
			try {
				setHubProxyPass(config.getProxyInfo().getDecryptedPassword());
			} catch (final IllegalArgumentException e) {
				setHubProxyPass(config.getProxyInfo().getMaskedPassword());
			} catch (final EncryptionException e) {
				setHubProxyPass(config.getProxyInfo().getMaskedPassword());
			}
		}

		setHubConfigMode(HUB_CONFIG_MODE_SUBMIT);
	}

	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;

		if (this.configManager != null) {

			HubServerConfig config;
			try {
				config = configManager.readConfig();
				updateLocalMembers(config);
			} catch (final MalformedURLException e) {
				logger.warn(READING_PERSISTED_CONFIG_WARNING, e);
			} catch (final IllegalArgumentException e) {
				logger.warn(READING_PERSISTED_CONFIG_WARNING, e);
			} catch (final HubIntegrationException e) {
				logger.warn(READING_PERSISTED_CONFIG_WARNING, e);
			} catch (final EncryptionException e) {
				logger.warn(READING_PERSISTED_CONFIG_WARNING, e);
			}

			// configure the Restlet engine so that the HTTPHandle and classes
			// from the com.sun.net.httpserver package
			// do not need to be used at runtime to make client calls.
			// DO NOT REMOVE THIS or the OSGI bundle will throw a
			// ClassNotFoundException for com.sun.net.httpserver.HttpHandler.
			// Since we are acting as a client we do not need the httpserver
			// components.

			// This workaround found here:
			// http://stackoverflow.com/questions/25179243/com-sun-net-httpserver-httphandler-classnotfound-exception-on-java-embedded-runt

			Engine.register(false);
			Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
		}
	}

	@Override
	public void validate() {
		clearErrorsAndMessages();

		final HubServerConfigBuilder config = new HubServerConfigBuilder(true);
		config.setHubUrl(getHubUrl());
		config.setUsername(getHubUser());
		config.setPassword(getHubPass());
		config.setProxyHost(getHubProxyUrl());
		config.setProxyPort(getHubProxyPort());
		config.setProxyUsername(getHubProxyUser());
		config.setProxyPassword(getHubProxyPass());
		config.setIgnoredProxyHosts(getHubNoProxyHost());
		final ValidationResults<GlobalFieldKey, HubServerConfig> result = config.build();
		if (!result.isSuccess()) {
			List<ValidationResult> resultList = null;

			resultList = result.getResultList(HubCredentialsFieldEnum.USERNAME);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubUser", getText("blackduckhub.action.config.validation.error.hub.user"));
			}

			resultList = result.getResultList(HubCredentialsFieldEnum.PASSWORD);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubPass", getText("blackduckhub.action.config.validation.error.hub.password"));
			}
			resultList = result.getResultList(HubServerConfigFieldEnum.HUBURL);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url"));
			}
			resultList = result.getResultList(HubProxyInfoFieldEnum.PROXYPORT);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubProxyPort", getText("blackduckhub.action.config.validation.error.proxy.port"));
			}

			resultList = result.getResultList(HubProxyInfoFieldEnum.PROXYUSERNAME);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubProxyUser", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
			}

			resultList = result.getResultList(HubProxyInfoFieldEnum.PROXYPASSWORD);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubProxyPass", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
			}

			resultList = result.getResultList(HubProxyInfoFieldEnum.NOPROXYHOSTS);
			if (resultList != null && !resultList.isEmpty()) {
				addFieldError("hubNoProxyHost",
						getText("blackduckhub.action.config.validation.error.proxy.host.ignore"));
			}
		}
	}

	private HubServerConfig createHubConfigInstance()
			throws IllegalArgumentException, MalformedURLException, HubIntegrationException, EncryptionException {

		return HubBambooUtils.getInstance().buildConfigFromStrings(getHubUrl(), getHubUser(), getHubPass(),
				getHubProxyUrl(), getHubProxyPort(), getHubNoProxyHost(), getHubProxyUser(), getHubProxyPass(),
				new IntBufferedLogger());
	}

	public String doSave() {

		HubServerConfig config;
		try {
			config = createHubConfigInstance();

			if (HUB_CONFIG_MODE_SUBMIT.equals(getHubConfigMode())) {
				try {
					configManager.writeConfig(config);
					addActionMessage(getText("blackduckhub.action.config.save.success"));
				} catch (final NoSuchMethodException e) {
					addActionError(e.getMessage());
				} catch (final IllegalAccessException e) {
					addActionError(e.getMessage());
				} catch (final IllegalArgumentException e) {
					addActionError(e.getMessage());
				} catch (final InvocationTargetException e) {
					addActionError(e.getMessage());
				} catch (final EncryptionException e) {
					addActionError(e.getMessage());
				}
			} else if (HUB_CONFIG_MODE_TEST.equals(getHubConfigMode())) {
				doTestConnection();
			}
			return SUCCESS;
		} catch (final IllegalArgumentException ex) {
			addActionError(ex.getMessage());
			return ERROR;
		} catch (final MalformedURLException ex) {
			addActionError(ex.getMessage());
			return ERROR;
		} catch (final HubIntegrationException ex) {
			addActionError(ex.getMessage());
			return ERROR;
		} catch (final EncryptionException ex) {
			addActionError(ex.getMessage());
			return ERROR;
		}
	}

	public String doTestConnection() {

		// these are local fields of this class they are not encrypted. May need
		// the encryption service with the
		// set and get methods. Think about it because if someone attached a
		// debugger they could see the values.
		try {
			final HubServerConfig hubConfig = createHubConfigInstance();
			final HubIntRestService service = new HubIntRestService(hubConfig.getHubUrl().toString());
			HubBambooUtils.getInstance().configureProxyToService(hubConfig, service);
			service.setCookies(getHubUser(), getHubPass());
			addActionMessage(getText("blackduckhub.action.config.test.success"));
		} catch (final HubIntegrationException ex) {
			handleTestConnectionError(ex);
		} catch (final URISyntaxException ex) {
			handleTestConnectionError(ex);
		} catch (final BDRestException ex) {
			handleTestConnectionError(ex);
		} catch (final Exception ex) {
			handleTestConnectionError(ex);
		}

		return SUCCESS;
	}

	private void handleTestConnectionError(final Exception ex) {
		addActionError(getText("blackduckhub.action.config.test.failed") + ex.getMessage());
		logger.error(TEST_CONNECTION_FAILED, ex);
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

	public String getHubConfigMode() {
		return this.hubConfigMode;
	}

	public void setHubConfigMode(final String hubConfigMode) {
		this.hubConfigMode = hubConfigMode;
	}
}
