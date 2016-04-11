package com.blackducksoftware.integration.hub.bamboo.config;

import java.net.Proxy;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;

public class HubServiceUtils {

	private final static HubServiceUtils instance = new HubServiceUtils();

	public static HubServiceUtils getInstance() {
		return instance;
	}

	private HubServiceUtils() {

	}

	public HubProxyInfo createProxyInfo(final HubConfig hubConfig) {
		final HubProxyInfo proxyInfo = new HubProxyInfo();
		proxyInfo.setHost(hubConfig.getHubProxyUrl());
		if (StringUtils.isNotBlank(hubConfig.getHubProxyPort())) {
			proxyInfo.setPort(Integer.valueOf(hubConfig.getHubProxyPort()));
		}
		proxyInfo.setIgnoredProxyHosts(hubConfig.getHubNoProxyHost());
		proxyInfo.setProxyUsername(hubConfig.getHubProxyUser());
		proxyInfo.setProxyPassword(hubConfig.getHubProxyPass());

		return proxyInfo;
	}

	public void configureProxyToService(final HubConfig hubConfig, final HubIntRestService service) {

		final HubProxyInfo proxyInfo = createProxyInfo(hubConfig);
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
}
