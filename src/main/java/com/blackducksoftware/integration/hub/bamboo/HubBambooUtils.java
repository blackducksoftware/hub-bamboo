package com.blackducksoftware.integration.hub.bamboo;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;
import com.blackducksoftware.integration.hub.bamboo.config.HubProxyInfo;

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
