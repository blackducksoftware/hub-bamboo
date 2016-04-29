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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.HttpClientHelper;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.ValidationExceptionEnum;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.valdators.HubBambooCredentialsValidator;
import com.blackducksoftware.integration.hub.bamboo.config.valdators.HubBambooProxyInfoValidator;
import com.blackducksoftware.integration.hub.bamboo.config.valdators.HubBambooServerConfigValidator;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ValidationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigBuilder;
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

		final HubBambooCredentialsValidator credentialvalidator = new HubBambooCredentialsValidator();
		ValidationException validationEx;
		try {
			validationEx = credentialvalidator.validateUserName(getHubUser());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				addFieldError("hubUser", getText("blackduckhub.action.config.validation.error.hub.user"));
			}
		} catch (final IOException ex) {
			addFieldError("hubUser", getText("blackduckhub.action.config.validation.error.hub.user"));
		}

		try {
			validationEx = credentialvalidator.validatePassword(getHubPass());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				addFieldError("hubPass", getText("blackduckhub.action.config.validation.error.hub.password"));
			}
		} catch (final IOException ex) {
			addFieldError("hubPass", getText("blackduckhub.action.config.validation.error.hub.password"));
		}

		final HubBambooServerConfigValidator validator = new HubBambooServerConfigValidator();
		try {
			validationEx = validator.validateServerUrl(getHubUrl());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url"));
			}
		} catch (final IOException ex) {
			addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url"));
		}

		boolean proxyInfoValid = true;
		final HubBambooProxyInfoValidator proxyValidator = new HubBambooProxyInfoValidator();

		try {
			validationEx = proxyValidator.validatePort(getHubProxyUrl(), getHubProxyPort());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				proxyInfoValid = false;
				addFieldError("hubProxyPort", getText("blackduckhub.action.config.validation.error.proxy.port"));
			}
		} catch (final IOException ex) {
			addFieldError("hubProxyPort", getText("blackduckhub.action.config.validation.error.proxy.port"));
		}

		try {
			validationEx = proxyValidator.validateCredentials(getHubProxyUrl(), getHubProxyUser(), getHubProxyPass());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				proxyInfoValid = false;
				addFieldError("hubProxyUser", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
				addFieldError("hubProxyPass", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
			}
		} catch (final IOException ex) {
			addFieldError("hubProxyUser", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
			addFieldError("hubProxyPass", getText("blackduckhub.action.config.validation.error.proxy.credentials"));
		}
		try {
			validationEx = proxyValidator.validateIgnoreHosts(getHubProxyUrl(), getHubNoProxyHost());
			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {
				proxyInfoValid = false;
				addFieldError("hubNoProxyHost",
						getText("blackduckhub.action.config.validation.error.proxy.host.ignore"));
			}
		} catch (final IOException ex) {
			addFieldError("hubNoProxyHost", getText("blackduckhub.action.config.validation.error.proxy.host.ignore"));
		}
		HubProxyInfo proxyInfo = null;

		if (proxyInfoValid) {

			final IntBufferedLogger bufferedLogger = new IntBufferedLogger();
			try {
				proxyInfo = HubBambooUtils.getInstance().buildProxyInfoFromString(getHubProxyUrl(), getHubProxyPort(),
						getHubNoProxyHost(), getHubProxyUser(), getHubProxyPass(), bufferedLogger);
			} catch (final IllegalArgumentException e) {
				proxyInfo = null;
			} catch (final EncryptionException e) {
				proxyInfo = null;
			} catch (final HubIntegrationException e) {
				proxyInfo = null;
			}
		}

		try {
			validationEx = validator.validateServerUrl(getHubUrl(), proxyInfo);

			if (validationEx != null && validationEx.getValidationExceptionEnum() == ValidationExceptionEnum.ERROR) {

				final String message = validationEx.getMessage();

				if (message != null) {
					if (message.startsWith(HubServerConfigBuilder.ERROR_MSG_UNREACHABLE_PREFIX)) {
						addFieldError("hubUrl",
								getText("blackduckhub.action.config.validation.error.hub.url.unreachable") + message);
					} else if (message.startsWith(HubServerConfigBuilder.ERROR_MSG_URL_NOT_VALID_PREFIX)) {
						addFieldError("hubUrl",
								getText("blackduckhub.action.config.validation.error.hub.url.default") + message);
					}
				} else {
					addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url.default"));
				}
			}
		} catch (final IOException e) {
			addFieldError("hubUrl", getText("blackduckhub.action.config.validation.error.hub.url.default"));
		}
	}

	// private void validateUrl(final String url) {
	//
	// URL testUrl = null;
	// try {
	// testUrl = new URL(url);
	// try {
	// testUrl.toURI();
	// } catch (final URISyntaxException e) {
	// addFieldError("hubUrl",
	// getText("blackduckhub.action.config.validation.error.hub.url.syntax") +
	// e.toString());
	// }
	// } catch (final MalformedURLException e) {
	// addFieldError("hubUrl",
	// getText("blackduckhub.action.config.validation.error.hub.url.syntax") +
	// e.toString());
	// }
	// if (testUrl != null) {
	// try {
	// if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
	// // If this property is not set the default is 20
	// // When not set the Authenticator redirects in a loop and
	// // results in an error for too many redirects
	// System.setProperty("http.maxRedirects", "3");
	// }
	// Proxy proxy = null;
	//
	// final HubServerConfig hubConfig = createHubConfigInstance();
	// final HubProxyInfo proxyInfo = hubConfig.getProxyInfo();
	//
	// if (StringUtils.isNotBlank(proxyInfo.getHost())
	// && StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
	// for (final Pattern p : proxyInfo.getNoProxyHostPatterns()) {
	// if (p.matcher(proxyInfo.getHost()).matches()) {
	// proxy = Proxy.NO_PROXY;
	// }
	// }
	// }
	// if (proxy == null && StringUtils.isNotBlank(proxyInfo.getHost()) &&
	// proxyInfo.getPort() != null) {
	// proxy = new Proxy(Proxy.Type.HTTP, new
	// InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));
	// }
	// attemptResetProxyCache();
	// if (proxy != null && proxy != Proxy.NO_PROXY) {
	//
	// if (StringUtils.isNotBlank(getHubProxyUser()) &&
	// StringUtils.isNotBlank(getHubProxyPass())) {
	// Authenticator.setDefault(new Authenticator() {
	// @Override
	// public PasswordAuthentication getPasswordAuthentication() {
	// return new PasswordAuthentication(getHubProxyUser(),
	// getHubProxyPass().toCharArray());
	// }
	// });
	// } else {
	// Authenticator.setDefault(null);
	// }
	// }
	// URLConnection connection = null;
	// if (proxy != null) {
	// connection = testUrl.openConnection(proxy);
	// } else {
	// connection = testUrl.openConnection();
	// }
	//
	// connection.getContent();
	// } catch (final IOException ioe) {
	// addFieldError("hubUrl",
	// getText("blackduckhub.action.config.validation.error.hub.url.unreachable")
	// + ioe.toString());
	// } catch (final RuntimeException e) {
	// addFieldError("hubUrl",
	// getText("blackduckhub.action.config.validation.error.hub.url.default") +
	// e.toString());
	// }
	// }
	//
	// }
	//
	// private void validateProxySettings() {
	//
	// final String proxyPort = getHubProxyPort();
	// if (StringUtils.isNotBlank(proxyPort)) {
	// try {
	// final int port = Integer.valueOf(proxyPort);
	// if (StringUtils.isNotBlank(getHubProxyUrl()) && port < 0) {
	// addFieldError("hubProxyPort",
	// getText("blackduckhub.action.config.validation.error.proxy.port"));
	// }
	// } catch (final NumberFormatException e) {
	// addFieldError("hubProxyPort",
	// getText("blackduckhub.action.config.validation.error.proxy.port") +
	// e.toString());
	// }
	// }
	// final String noProxyHosts = getHubNoProxyHost();
	// if (StringUtils.isNotBlank(noProxyHosts)) {
	// String[] ignoreHosts = null;
	// final List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
	//
	// if (StringUtils.isNotBlank(noProxyHosts)) {
	// if (noProxyHosts.contains(",")) {
	// ignoreHosts = noProxyHosts.split(",");
	// for (final String ignoreHost : ignoreHosts) {
	// try {
	// final Pattern pattern = Pattern.compile(ignoreHost);
	// noProxyHostsPatterns.add(pattern);
	// } catch (final PatternSyntaxException e) {
	//
	// final StringBuilder sb = new StringBuilder(100);
	// sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.prefix"));
	// sb.append(ignoreHost);
	// sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.suffix"));
	// addFieldError("hubNoProxyHost", sb.toString());
	// }
	// }
	// } else {
	// try {
	// final Pattern pattern = Pattern.compile(noProxyHosts);
	// noProxyHostsPatterns.add(pattern);
	// } catch (final PatternSyntaxException e) {
	//
	// final StringBuilder sb = new StringBuilder(100);
	// sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.prefix"));
	// sb.append(noProxyHosts);
	// sb.append(getText("blackduckhub.action.config.validation.error.proxy.host.ignore.suffix"));
	// addFieldError("hubNoProxyHost", sb.toString());
	// }
	// }
	// }
	// }
	// }
	//
	// private void attemptResetProxyCache() {
	// try {
	// // works, and resets the cache when using sun classes
	// // sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new
	// // sun.net.www.protocol.http.AuthCacheImpl());
	//
	// // Attempt the same thing using reflection in case they are not
	// // using a jdk with sun classes
	//
	// Class<?> sunAuthCacheValue;
	// Class<?> sunAuthCache;
	// Class<?> sunAuthCacheImpl;
	// try {
	// sunAuthCacheValue =
	// Class.forName("sun.net.www.protocol.http.AuthCacheValue");
	// sunAuthCache = Class.forName("sun.net.www.protocol.http.AuthCache");
	// sunAuthCacheImpl =
	// Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
	// } catch (final Exception e) {
	// // Must not be using a JDK with sun classes so we abandon this
	// // reset since it is sun specific
	// return;
	// }
	//
	// final Method m = sunAuthCacheValue.getDeclaredMethod("setAuthCache",
	// sunAuthCache);
	//
	// final Constructor<?> authCacheImplConstr =
	// sunAuthCacheImpl.getConstructor();
	// final Object authCachImp = authCacheImplConstr.newInstance();
	//
	// m.invoke(null, authCachImp);
	//
	// } catch (final Exception e) {
	// logger.error(e);
	// }
	// }

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
