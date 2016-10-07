package com.blackducksoftware.integration.hub.bamboo;

import com.atlassian.plugin.PluginAccessor;

public class HubBambooPluginHelper {

	private final PluginAccessor pluginAccessor;

	public HubBambooPluginHelper(final PluginAccessor pluginAccessor) {
		this.pluginAccessor = pluginAccessor;
	}

	public String getPluginVersion() {
		return pluginAccessor.getPlugin("com.blackducksoftware.integration.hub-bamboo").getPluginInformation()
				.getVersion();
	}
}
