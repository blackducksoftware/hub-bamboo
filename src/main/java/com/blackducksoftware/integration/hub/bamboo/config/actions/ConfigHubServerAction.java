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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;
import com.blackducksoftware.integration.hub.bamboo.config.HubProxyInfo;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class ConfigHubServerAction extends BambooActionSupport implements GlobalAdminSecurityAware {

	private static final long serialVersionUID = 4380000697000607530L;

	private final Logger logger = Logger.getLogger(ConfigHubServerAction.class);

	private final static String HUB_CONFIG_MODE = "submit";

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

	private void updateLocalMembers(final HubConfig config) {
		setHubUrl(config.getHubUrl());
		setHubUser(config.getHubUser());
		setHubPass(config.getHubPass());

		setHubProxyUrl(config.getHubProxyUrl());
		setHubProxyPort(config.getHubProxyPort());
		setHubNoProxyHost(config.getHubNoProxyHost());
		setHubProxyUser(config.getHubProxyUser());
		setHubProxyPass(config.getHubProxyPass());
		setHubConfigMode(HUB_CONFIG_MODE);
	}

	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;

		if (this.configManager != null) {
			final HubConfig config = configManager.readConfig();
			updateLocalMembers(config);

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
		if (StringUtils.isBlank(getHubUser())) {
			addFieldError("hubUser", getText("blackduckhub.action.config.validation.error.hub.user"));
		}
		if (StringUtils.isBlank(getHubPass())) {
			addFieldError("hubPass", getText("blackduckhub.action.config.validation.error.hub.password"));
		}

		if (StringUtils.isBlank(getHubUrl())) {
			addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url"));
		}

		validateProxySettings();

		if (StringUtils.isNotBlank(getHubUrl())) {
			validateUrl(getHubUrl());
		}
	}

	private void validateUrl(final String url) { // , boolean isTestConnection)
													// {

		URL testUrl = null;
		try {
			testUrl = new URL(url);
			try {
				testUrl.toURI();
			} catch (final URISyntaxException e) {
				addFieldError("hubUrl",
						getText("blackduckhub.action.config.validation.error.hub.url.syntax") + e.toString());
			}
		} catch (final MalformedURLException e) {
			addFieldError("hubUrl",
					getText("blackduckhub.action.config.validation.error.hub.url.syntax") + e.toString());
		}
		// if (isTestConnection) {
		if (testUrl != null) {
			try {
				if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
					// If this property is not set the default is 20
					// When not set the Authenticator redirects in a loop and
					// results in an error for too many redirects
					System.setProperty("http.maxRedirects", "3");
				}
				Proxy proxy = null;

				final HubConfig hubConfig = createHubConfigInstance();
				final HubProxyInfo proxyInfo = HubBambooUtils.getInstance().createProxyInfo(hubConfig);

				if (StringUtils.isNotBlank(proxyInfo.getHost())
						&& StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
					for (final Pattern p : proxyInfo.getNoProxyHostPatterns()) {
						if (p.matcher(proxyInfo.getHost()).matches()) {
							proxy = Proxy.NO_PROXY;
						}
					}
				}
				if (proxy == null && StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != null) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));
				}
				attemptResetProxyCache();
				if (proxy != null && proxy != Proxy.NO_PROXY) {

					if (StringUtils.isNotBlank(getHubProxyUser()) && StringUtils.isNotBlank(getHubProxyPass())) {
						Authenticator.setDefault(new Authenticator() {
							@Override
							public PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(getHubProxyUser(), getHubProxyPass().toCharArray());
							}
						});
					} else {
						Authenticator.setDefault(null);
					}
				}
				URLConnection connection = null;
				if (proxy != null) {
					connection = testUrl.openConnection(proxy);
				} else {
					connection = testUrl.openConnection();
				}

				connection.getContent();
			} catch (final IOException ioe) {
				addFieldError("hubUrl",
						getText("blackduckhub.action.config.validation.error.hub.url.unreachable") + ioe.toString());
			} catch (final RuntimeException e) {
				addFieldError("hubUrl",
						getText("blackduckhub.action.config.validation.error.hub.url.default") + e.toString());
			}
		}

	}

	private void validateProxySettings() {

		final String proxyPort = getHubProxyPort();
		if (StringUtils.isNotBlank(proxyPort)) {
			try {
				final int port = Integer.valueOf(proxyPort);
				if (StringUtils.isNotBlank(getHubProxyUrl()) && port < 0) {
					addFieldError("hubProxyPort", getText("blackduckhub.action.config.validation.error.proxy.port"));
				}
			} catch (final NumberFormatException e) {
				addFieldError("hubProxyPort",
						getText("blackduckhub.action.config.validation.error.proxy.port") + e.toString());
			}
		}
		final String noProxyHosts = getHubNoProxyHost();
		if (StringUtils.isNotBlank(noProxyHosts)) {
			String[] ignoreHosts = null;
			final List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();

			if (StringUtils.isNotBlank(noProxyHosts)) {
				if (noProxyHosts.contains(",")) {
					ignoreHosts = noProxyHosts.split(",");
					for (final String ignoreHost : ignoreHosts) {
						try {
							final Pattern pattern = Pattern.compile(ignoreHost);
							noProxyHostsPatterns.add(pattern);
						} catch (final PatternSyntaxException e) {

							final StringBuilder sb = new StringBuilder(100);
							sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.prefix"));
							sb.append(ignoreHost);
							sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.suffix"));
							addFieldError("hubNoProxyHost", sb.toString());
						}
					}
				} else {
					try {
						final Pattern pattern = Pattern.compile(noProxyHosts);
						noProxyHostsPatterns.add(pattern);
					} catch (final PatternSyntaxException e) {

						final StringBuilder sb = new StringBuilder(100);
						sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.prefix"));
						sb.append(noProxyHosts);
						sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.suffix"));
						addFieldError("hubNoProxyHost", sb.toString());
					}
				}
			}
		}
	}

	private void attemptResetProxyCache() {
		try {
			// works, and resets the cache when using sun classes
			// sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new
			// sun.net.www.protocol.http.AuthCacheImpl());

			// Attempt the same thing using reflection in case they are not
			// using a jdk with sun classes

			Class<?> sunAuthCacheValue;
			Class<?> sunAuthCache;
			Class<?> sunAuthCacheImpl;
			try {
				sunAuthCacheValue = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
				sunAuthCache = Class.forName("sun.net.www.protocol.http.AuthCache");
				sunAuthCacheImpl = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
			} catch (final Exception e) {
				// Must not be using a JDK with sun classes so we abandon this
				// reset since it is sun specific
				return;
			}

			final Method m = sunAuthCacheValue.getDeclaredMethod("setAuthCache", sunAuthCache);

			final Constructor<?> authCacheImplConstr = sunAuthCacheImpl.getConstructor();
			final Object authCachImp = authCacheImplConstr.newInstance();

			m.invoke(null, authCachImp);

		} catch (final Exception e) {
			logger.error(e);
		}
	}

	private HubConfig createHubConfigInstance() {
		return new HubConfig(getHubUrl(), getHubUser(), getHubPass(), getHubProxyUrl(), getHubProxyPort(),
				getHubNoProxyHost(), getHubProxyUser(), getHubProxyPass());
	}

	public String doSave() {

		final HubConfig config = createHubConfigInstance();

		if (HUB_CONFIG_MODE.equals(getHubConfigMode())) {
			configManager.writeConfig(config);
			addActionMessage(getText("blackduckhub.action.config.save.success")); // internationalize
																					// it.

		} else {
			doTestConnection();
		}

		return SUCCESS;
	}

	public String doTestConnection() {

		// these are local fields of this class they are not encrypted. May need
		// the encryption service with the
		// set and get methods. Think about it because if someone attached a
		// debugger they could see the values.
		try {
			final HubConfig hubConfig = createHubConfigInstance();
			final HubIntRestService service = new HubIntRestService(hubConfig.getHubUrl());
			HubBambooUtils.getInstance().configureProxyToService(hubConfig, service);
			service.setCookies(getHubUser(), getHubPass());
			addActionMessage(getText("blackduckhub.action.config.test.success"));
		} catch (final HubIntegrationException ex) {
			handleTestConnectionError(ex);
		} catch (final URISyntaxException ex) {
			handleTestConnectionError(ex);
		} catch (final BDRestException ex) {
			handleTestConnectionError(ex);
		}

		return SUCCESS;
	}

	private void handleTestConnectionError(final Exception ex) {
		addActionError(getText("blackduckhub.action.config.test.failed") + ex.getMessage());
		logger.error("Test Connection Failed", ex);
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
