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
package com.blackducksoftware.integration.hub.bamboo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubProxyInfoBuilder;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubBambooUtils implements Cloneable {

	private final static HubBambooUtils instance = new HubBambooUtils();

	public static HubBambooUtils getInstance() {
		return instance;
	}

	private HubBambooUtils() {

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public HubServerConfig buildConfigFromStrings(final String hubUrl, final String hubUser, final String hubPass,
			final String hubProxyUrl, final String hubProxyPort, final String hubProxyNoHost, final String hubProxyUser,
			final String hubProxyPass, final IntLogger logger)
			throws IllegalArgumentException, HubIntegrationException, EncryptionException, MalformedURLException {
		final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();

		final HubCredentialsBuilder credentialBuilder = new HubCredentialsBuilder();
		credentialBuilder.setUsername(hubUser);
		credentialBuilder.setPassword(hubPass);

		HubProxyInfo proxyInfo = null;
		if (StringUtils.isNotBlank(hubProxyUrl)) {
			final HubProxyInfoBuilder proxyBuilder = new HubProxyInfoBuilder();
			proxyBuilder.setHost(hubProxyUrl);
			if (StringUtils.isNotBlank(hubProxyPort)) {
				try {
					proxyBuilder.setPort(Integer.valueOf(hubProxyPort));
				} catch (final NumberFormatException ex) {
					// ignore the default value is 0.
				}

				proxyBuilder.setIgnoredProxyHosts(hubProxyNoHost);
				proxyBuilder.setUsername(hubProxyUser);
				proxyBuilder.setPassword(hubProxyPass);
				proxyInfo = proxyBuilder.build();
			}
		}
		configBuilder.setHubUrl(hubUrl);
		configBuilder.setCredentials(credentialBuilder.build(logger));
		configBuilder.setProxyInfo(proxyInfo);

		return configBuilder.build(logger);
	}

	public void configureProxyToService(final HubServerConfig hubConfig, final HubIntRestService service)
			throws MalformedURLException {

		final HubProxyInfo proxyInfo = hubConfig.getProxyInfo();

		if (proxyInfo != null && (proxyInfo.shouldUseProxyForUrl(new URL(proxyInfo.getHost())))) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getUsername())
						&& StringUtils.isNotBlank(proxyInfo.getEncryptedPassword())) {
					service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null, proxyInfo.getUsername(),
							proxyInfo.getEncryptedPassword());
				} else {
					service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null, null, null);
				}
			}
		}
	}

	public List<String> createScanTargetPaths(final String targetPathText, final File workingDirectory) {

		final List<String> scanTargets = new ArrayList<String>();

		if (StringUtils.isNotBlank(targetPathText)) {
			final String[] scanTargetPathsArray = targetPathText.split("\\r?\\n");
			for (final String target : scanTargetPathsArray) {
				if (!StringUtils.isBlank(target)) {
					if (workingDirectory != null && StringUtils.isBlank(workingDirectory.getAbsolutePath())) {
						scanTargets.add(target);

					} else {
						scanTargets.add(new File(workingDirectory, target).getAbsolutePath());
					}
				}
			}
		}

		return scanTargets;
	}
}
