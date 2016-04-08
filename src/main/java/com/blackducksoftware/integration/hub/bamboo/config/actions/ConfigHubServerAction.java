package com.blackducksoftware.integration.hub.bamboo.config.actions;

import java.io.IOException;
import java.net.Authenticator;
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

import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.spring.ComponentAccessor;
import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.atlassian.bamboo.ww2.aware.permissions.GlobalAdminSecurityAware;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;
import com.blackducksoftware.integration.hub.bamboo.config.HubProxyInfo;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class ConfigHubServerAction extends BambooActionSupport implements GlobalAdminSecurityAware {

	private static final long serialVersionUID = 4380000697000607530L;

	private transient Logger logger = Logger.getLogger(ConfigHubServerAction.class);

	private EncryptionService encryptionService;

	private String hubUrl;
	private String hubUser;
	private String hubPass;
	private String hubProxyUrl;
	private String hubProxyPort;
	private String hubNoProxyHost;
	private String hubProxyUser;
	private String hubProxyPass;

	private transient ConfigManager configManager;

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
			final HubConfig config = configManager.readConfig();
			updateLocalMembers(config);
			encryptionService = ComponentAccessor.ENCRYPTION_SERVICE.get();
		}
	}

	@Override
	public void validate() {

		if (StringUtils.isBlank(getHubUser())) {
			addError("errorUserName", "Please specify a UserName.");
		}
		if (StringUtils.isBlank(getHubPass())) {
			addError("errorPassword", "There is no saved Password. Please specify a Password.");
		}

		if (StringUtils.isBlank(getHubUrl())) {
			addError("errorUrl", "Please specify a URL.");
		}

		validateProxySettings();
		validateUrl(getHubUrl());

		// use hub ci common validation here....
		super.validate();
	}

	private void validateUrl(final String url) { // , boolean isTestConnection)
													// {

		URL testUrl = null;
		try {
			testUrl = new URL(url);
			try {
				testUrl.toURI();
			} catch (final URISyntaxException e) {
				addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
			}
		} catch (final MalformedURLException e) {
			addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
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
				final Proxy proxy = null;

				// if (StringUtils.isNotBlank(getHubProxyUrl())
				// && StringUtils.isNotBlank(getHubNoProxyHost())) {
				// for (final Pattern p : getHubNoProxyHost()) {
				// if (p.matcher(proxyInfo.getHost()).matches()) {
				// proxy = Proxy.NO_PROXY;
				// }
				// }
				// }
				// if (proxy == null &&
				// StringUtils.isNotBlank(proxyInfo.getHost()) &&
				// proxyInfo.getPort() != null) {
				// proxy = new Proxy(Proxy.Type.HTTP,
				// new InetSocketAddress(proxyInfo.getHost(),
				// proxyInfo.getPort()));
				// }
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
				addError("errorUrl", "Trouble reaching the Hub server. " + ioe.toString());
			} catch (final RuntimeException e) {
				addError("errorUrl", "Not a valid Hub server. " + e.toString());
			}
		}
	}

	private void validateProxySettings() {

		final String proxyPort = getHubProxyPort();
		if (StringUtils.isNotBlank(proxyPort)) {
			try {
				final int port = Integer.valueOf(proxyPort);
				if (StringUtils.isNotBlank(getHubProxyUrl()) && port < 0) {
					addError("errorHubProxyPort", "Please enter a valid Proxy port.");
				}
			} catch (final NumberFormatException e) {
				addError("errorHubProxyPort", "Please enter a valid Proxy port. " + e.toString());
			}
		}
		final String noProxyHosts = getHubNoProxyHost();
		if (StringUtils.isNotBlank(noProxyHosts)) {
			String[] ignoreHosts = null;
			final List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
			boolean patternError = false;
			if (StringUtils.isNotBlank(noProxyHosts)) {
				if (noProxyHosts.contains(",")) {
					ignoreHosts = noProxyHosts.split(",");
					for (final String ignoreHost : ignoreHosts) {
						try {
							final Pattern pattern = Pattern.compile(ignoreHost);
							noProxyHostsPatterns.add(pattern);
						} catch (final PatternSyntaxException e) {
							patternError = true;
							addError("errorHubNoProxyHost",
									"The host : " + ignoreHost + " : is not a valid regular expression.");
						}
					}
				} else {
					try {
						final Pattern pattern = Pattern.compile(noProxyHosts);
						noProxyHostsPatterns.add(pattern);
					} catch (final PatternSyntaxException e) {
						patternError = true;
						addError("errorHubNoProxyHost",
								"The host : " + noProxyHosts + " : is not a valid regular expression.");
					}
				}
			}

		}
	}

	public String doEditConfig() {

		final HubConfig config = new HubConfig(getHubUrl(), getHubUser(), getHubPass(), getHubProxyUrl(),
				getHubProxyPort(), getHubNoProxyHost(), getHubProxyUser(), getHubProxyPass());
		configManager.writeConfig(config);

		logInputValues();

		return SUCCESS;
	}

	public String doTestConnection() {

		// these are local fields of this class they are not encrypted. May need
		// the encryption service with the
		// set and get methods. Think about it because if someone attached a
		// debugger they could see the values.
		try {
			logInputValues();

			final HubIntRestService service = new HubIntRestService(getHubUrl());
			configureProxyToService(service);
			service.setCookies(getHubUser(), getHubPass());
			addActionMessage("Connection Successful!"); // internationalize it.
		} catch (final HubIntegrationException ex) {
			handleTestConnectionError(ex);
		} catch (final URISyntaxException ex) {
			handleTestConnectionError(ex);
		} catch (final BDRestException ex) {
			handleTestConnectionError(ex);
		}

		return SUCCESS;
	}

	private void logInputValues() {
		final HubConfig config = configManager.readConfig();
		logger.info("##### Input Parameters #####");
		final String buffer = "                 ";
		logger.info("      Local:     ");
		logger.info(buffer + " hubUrl:          " + getHubUrl());
		logger.info(buffer + " hubUser:         " + getHubUser());
		logger.info(buffer + " hubPass:         " + getHubPass());
		logger.info(buffer + " hubProxyUrl:     " + getHubProxyUrl());
		logger.info(buffer + " hubProxyPort:    " + getHubProxyPort());
		logger.info(buffer + " hubProxyNoHosts: " + getHubNoProxyHost());
		logger.info(buffer + " hubProxyUser:    " + getHubProxyUser());
		logger.info(buffer + " hubProxyPass:    " + getHubProxyPass());
		logger.info("      Persisted: ");
		logger.info(buffer + " hubUrl:          " + config.getHubUrl());
		logger.info(buffer + " hubUser:         " + config.getHubUser());
		logger.info(buffer + " hubPass:         " + config.getHubPass());
		logger.info(buffer + " hubProxyUrl:     " + config.getHubProxyUrl());
		logger.info(buffer + " hubProxyPort:    " + config.getHubProxyPort());
		logger.info(buffer + " hubProxyNoHosts: " + config.getHubNoProxyHost());
		logger.info(buffer + " hubProxyUser:    " + config.getHubProxyUser());
		logger.info(buffer + " hubProxyPass:    " + config.getHubProxyPass());
	}

	private void configureProxyToService(final HubIntRestService service) {

		final HubProxyInfo proxyInfo = new HubProxyInfo();
		proxyInfo.setHost(getHubProxyUrl());
		if (StringUtils.isNotBlank(getHubProxyPort())) {
			proxyInfo.setPort(Integer.valueOf(getHubProxyPort()));
		}
		proxyInfo.setIgnoredProxyHosts(getHubNoProxyHost());
		proxyInfo.setProxyUsername(getHubProxyUser());
		proxyInfo.setProxyPassword(getHubProxyPass());

		Proxy proxy = null;

		if (StringUtils.isNotBlank(proxyInfo.getHost()) && StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
			for (final Pattern p : proxyInfo.getNoProxyHostPatterns()) {
				if (p.matcher(proxyInfo.getHost()).matches()) {
					proxy = Proxy.NO_PROXY;
				}
			}
		}

		if (proxyInfo != null && (proxy == null || proxy != Proxy.NO_PROXY)) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())
						&& StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
					service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null,
							proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
				} else {
					service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null, null, null);
				}
			}
		}
	}

	private void handleTestConnectionError(final Exception ex) {

		addActionError("Connection Failed. Cause: " + ex.getMessage());
		logger.error("Test Connection Failed", ex);
	}

	public String getHubUrl() {
		return hubUrl;
	}

	public void setHubUrl(final String hubUrl) {
		this.hubUrl = hubUrl;
	}

	public String getHubUser() {
		return encryptionService.decrypt(hubUser);
	}

	public void setHubUser(final String hubUser) {
		this.hubUser = encryptionService.encrypt(hubUser);
	}

	public String getHubPass() {
		return encryptionService.decrypt(hubPass);
	}

	public void setHubPass(final String hubPass) {
		this.hubPass = encryptionService.encrypt(hubPass);
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
