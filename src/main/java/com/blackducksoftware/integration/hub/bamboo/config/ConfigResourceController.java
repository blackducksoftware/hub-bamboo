package com.blackducksoftware.integration.hub.bamboo.config;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;

@Path("/")
public class ConfigResourceController {

	private final UserManager userManager;
	private final PluginSettingsFactory pluginSettingsFactory;
	private final TransactionTemplate transactionTemplate;

	public ConfigResourceController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
			final TransactionTemplate transactionTemplate) {

		this.userManager = userManager;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.transactionTemplate = transactionTemplate;
	}

	private boolean isUserAuthorized(final HttpServletRequest request) {
		final String userName = userManager.getRemoteUsername(request);
		if (userName != null && userManager.isSystemAdmin(userName)) {
			return true;
		} else {
			return false;
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfig(@Context final HttpServletRequest request) {

		if (!isUserAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		return Response.ok(transactionTemplate.execute(new TransactionCallback() {

			@Override
			public Object doInTransaction() {
				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				final HubConfig hubConfig = new HubConfig();
				hubConfig.setHubUrl((String) settings.get(ConfigKeyEnum.HUB_URL.getKey()));
				hubConfig.setHubUser((String) settings.get(ConfigKeyEnum.HUB_USER.getKey()));
				hubConfig.setHubPass((String) settings.get(ConfigKeyEnum.HUB_PASS.getKey()));

				// proxy settings
				hubConfig.setHubProxyUrl((String) settings.get(ConfigKeyEnum.PROXY_URL.getKey()));
				hubConfig.setHubProxyPort((String) settings.get(ConfigKeyEnum.PROXY_PORT.getKey()));
				hubConfig.setHubNoProxyHost((String) settings.get(ConfigKeyEnum.PROXY_NOHOSTS.getKey()));
				hubConfig.setHubProxyUser((String) settings.get(ConfigKeyEnum.PROXY_USER.getKey()));
				hubConfig.setHubProxyPass((String) settings.get(ConfigKeyEnum.PROXY_PASS.getKey()));
				return hubConfig;
			}
		})).build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putConfig(final HubConfig hubConfig, @Context final HttpServletRequest request) {

		if (!isUserAuthorized(request)) {
			return Response.status(Status.UNAUTHORIZED).build();
		}

		transactionTemplate.execute(new TransactionCallback() {

			@Override
			public Object doInTransaction() {

				final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
				final HubConfig hubConfig = new HubConfig();
				settings.put(ConfigKeyEnum.HUB_URL.getKey(), hubConfig.getHubUrl());
				settings.put(ConfigKeyEnum.HUB_USER.getKey(), hubConfig.getHubUser());
				settings.put(ConfigKeyEnum.HUB_PASS.getKey(), hubConfig.getHubPass());

				// proxy settings
				settings.put(ConfigKeyEnum.PROXY_URL.getKey(), hubConfig.getHubProxyUrl());
				settings.put(ConfigKeyEnum.PROXY_PORT.getKey(), hubConfig.getHubProxyPort());
				settings.put(ConfigKeyEnum.PROXY_NOHOSTS.getKey(), hubConfig.getHubNoProxyHost());
				settings.put(ConfigKeyEnum.PROXY_USER.getKey(), hubConfig.getHubProxyUser());
				settings.put(ConfigKeyEnum.PROXY_PASS.getKey(), hubConfig.getHubProxyPass());

				return null;
			}
		});

		return Response.noContent().build();
	}
}
