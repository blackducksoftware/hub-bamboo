/**
 * Black Duck Hub Plugin for Bamboo
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.bamboo.config;

import java.io.PrintWriter;
import java.io.StringWriter;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.tasks.HubConfigKeys;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResults;

@Path("/")
public class HubConfigController {

    private final UserManager userManager;

    private final PluginSettingsFactory pluginSettingsFactory;

    private final TransactionTemplate transactionTemplate;

    private final PluginAccessor pluginAccessor;

    public HubConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
            final TransactionTemplate transactionTemplate, final PluginAccessor pluginAccessor) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.pluginAccessor = pluginAccessor;
    }

    private Response checkUserPermissions(final HttpServletRequest request, final PluginSettings settings) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (userManager.isSystemAdmin(username)) {
            return null;
        }

        final String hubJiraGroupsString = getValue(settings, HubConfigKeys.HUB_CONFIG_GROUPS);

        if (StringUtils.isNotBlank(hubJiraGroupsString)) {
            final String[] hubJiraGroups = hubJiraGroupsString.split(",");
            boolean userIsInGroups = false;
            for (final String hubJiraGroup : hubJiraGroups) {
                if (userManager.isUserInGroup(username, hubJiraGroup.trim())) {
                    userIsInGroups = true;
                    break;
                }
            }
            if (userIsInGroups) {
                return null;
            }
        }
        return Response.status(Status.UNAUTHORIZED).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final String hubUrl = getValue(settings, HubConfigKeys.CONFIG_HUB_URL);
                final String username = getValue(settings, HubConfigKeys.CONFIG_HUB_USER);
                final String password = getValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
                final String passwordLength = getValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
                final String timeout = getValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);
                final String proxyHost = getValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
                final String proxyPort = getValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
                final String noProxyHosts = getValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
                final String proxyUser = getValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
                final String proxyPassword = getValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
                final String proxyPasswordLength = getValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

                final String importSSLCertsString = getValue(settings, HubConfigKeys.CONFIG_HUB_IMPORT_SSL_CERTIFICATES);
                final String hubWorkspaceCheckString = getValue(settings, HubConfigKeys.CONFIG_HUB_WORKSPACE_CHECK);

                Boolean importSSLCerts = false;
                Boolean hubWorkspaceCheck = true;
                if (StringUtils.isNotBlank(importSSLCertsString)) {
                    importSSLCerts = Boolean.valueOf(importSSLCerts);
                }
                if (StringUtils.isNotBlank(hubWorkspaceCheckString)) {
                    hubWorkspaceCheck = Boolean.valueOf(hubWorkspaceCheckString);
                }

                final HubServerConfigSerializable config = new HubServerConfigSerializable();

                final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
                serverConfigBuilder.setHubUrl(hubUrl);
                serverConfigBuilder.setTimeout(timeout);
                serverConfigBuilder.setUsername(username);
                serverConfigBuilder.setPassword(password);
                serverConfigBuilder.setAutoImportHttpsCertificates(importSSLCerts);
                serverConfigBuilder.setPasswordLength(NumberUtils.toInt(passwordLength));
                serverConfigBuilder.setProxyHost(proxyHost);
                serverConfigBuilder.setProxyPort(proxyPort);
                serverConfigBuilder.setIgnoredProxyHosts(noProxyHosts);
                serverConfigBuilder.setProxyUsername(proxyUser);
                serverConfigBuilder.setProxyPassword(proxyPassword);
                serverConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(proxyPasswordLength));

                setConfigFromResult(config, serverConfigBuilder.createValidator());

                config.setHubUrl(hubUrl);
                config.setUsername(username);
                if (StringUtils.isNotBlank(password)) {
                    final int passwordLengthInt = getIntFromObject(passwordLength);
                    if (passwordLengthInt > 0) {
                        config.setPasswordLength(passwordLengthInt);
                        config.setPassword(config.getMaskedPassword());
                    }
                }
                config.setTimeout(timeout);
                config.setHubProxyHost(proxyHost);
                config.setHubProxyPort(proxyPort);
                config.setHubNoProxyHosts(noProxyHosts);
                config.setHubProxyUser(proxyUser);
                if (StringUtils.isNotBlank(proxyPassword)) {
                    final int hubProxyPasswordLength = getIntFromObject(proxyPasswordLength);
                    if (hubProxyPasswordLength > 0) {
                        config.setHubProxyPasswordLength(hubProxyPasswordLength);
                        config.setHubProxyPassword(config.getMaskedProxyPassword());
                    }
                }
                config.setHubWorkspaceCheck(hubWorkspaceCheck);

                checkAtlassianConfigInstalled(config);
                return config;
            }
        });

        return Response.ok(obj).build();
    }

    private void checkAtlassianConfigInstalled(final HubServerConfigSerializable config) {
        final Plugin atlassianConfigPlugin = pluginAccessor.getPlugin("com.blackducksoftware.integration.hub-atlassian-config");
        if (atlassianConfigPlugin != null) {
            config.setTestConnectionError("You have the Hub Atlassian Config installed, please un-install that plugin to reduce confusion.");
        }
    }

    private int getIntFromObject(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(value);
        }
        return 0;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final HubServerConfigSerializable config, @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final HubServerConfigBuilder serverConfigBuilder = setConfigBuilderFromSerializableConfig(config,
                        settings);

                setConfigFromResult(config, serverConfigBuilder.createValidator());

                setValue(settings, HubConfigKeys.CONFIG_HUB_URL, config.getHubUrl());
                setValue(settings, HubConfigKeys.CONFIG_HUB_USER, config.getUsername());

                final String password = config.getPassword();
                if (StringUtils.isNotBlank(password) && !config.isPasswordMasked()) {
                    // only update the stored password if it is not the masked
                    // password used for display
                    try {
                        final String encPassword = PasswordEncrypter.encrypt(password);
                        setValue(settings, HubConfigKeys.CONFIG_HUB_PASS, encPassword);
                        setValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH, String.valueOf(password.length()));
                    } catch (IllegalArgumentException | EncryptionException e) {
                    }
                } else if (StringUtils.isBlank(password)) {
                    setValue(settings, HubConfigKeys.CONFIG_HUB_PASS, null);
                    setValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH, null);
                }
                setValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT, config.getTimeout());
                setValue(settings, HubConfigKeys.CONFIG_PROXY_HOST, config.getHubProxyHost());
                setValue(settings, HubConfigKeys.CONFIG_PROXY_PORT, config.getHubProxyPort());
                setValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST, config.getHubNoProxyHosts());
                setValue(settings, HubConfigKeys.CONFIG_PROXY_USER, config.getHubProxyUser());

                setValue(settings, HubConfigKeys.CONFIG_HUB_IMPORT_SSL_CERTIFICATES, config.getImportSSLCerts());
                setValue(settings, HubConfigKeys.CONFIG_HUB_WORKSPACE_CHECK, config.getHubWorkspaceCheck());

                final String proxyPassword = config.getHubProxyPassword();
                if (StringUtils.isNotBlank(proxyPassword) && !config.isProxyPasswordMasked()) {
                    // only update the stored password if it is not the masked
                    // password used for display
                    try {
                        final String encryptedProxyPassword = PasswordEncrypter.encrypt(proxyPassword);
                        setValue(settings, HubConfigKeys.CONFIG_PROXY_PASS, encryptedProxyPassword);
                        setValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH,
                                String.valueOf(proxyPassword.length()));
                    } catch (IllegalArgumentException | EncryptionException e) {
                    }
                } else if (StringUtils.isBlank(proxyPassword)) {
                    setValue(settings, HubConfigKeys.CONFIG_PROXY_PASS, null);
                    setValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH, null);
                }
                return null;
            }
        });

        if (config.hasErrors()) {
            return Response.ok(config).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/testConnection")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testConnection(final HubServerConfigSerializable config,
            @Context final HttpServletRequest request) {
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final HubServerConfigBuilder serverConfigBuilder = setConfigBuilderFromSerializableConfig(config,
                            settings);

                    setConfigFromResult(config, serverConfigBuilder.createValidator());

                    if (config.hasErrors() && !config.getImportSSLCerts()) {
                        return config;
                    } else {
                        HubServerConfig serverConfig = null;
                        try {
                            try {
                                serverConfig = serverConfigBuilder.build();
                            } catch (final IllegalStateException e) {
                                if (e instanceof IntegrationCertificateException
                                        || e.getMessage().toLowerCase().contains("ssl")) {
                                    config.setHubUrlError("ERROR: Certificate could not be imported into the java keystore. "
                                            + "Please ensure the bamboo user has proper read/write access.");
                                } else {
                                    config.setHubUrlError(e.getMessage());
                                }
                            }
                            if (serverConfig != null) {
                                // if a cert was imported, we can connect and remove error messages
                                final RestConnection restConnection = HubBambooUtils.getInstance().getRestConnection(null,
                                        serverConfig);
                                restConnection.connect();
                                config.setHubUrlError(null);
                            }
                        } catch (final IntegrationException e) {
                            if (e.getMessage().toLowerCase().contains("unauthorized")) {
                                config.setUsernameError(
                                        "Username and Password are invalid for : " + config.getHubUrl());
                            } else {
                                config.setTestConnectionError(e.toString());
                            }
                        }
                        return config;
                    }
                }
            });
            if (config.hasErrors()) {
                return Response.ok(config).status(Status.BAD_REQUEST).build();
            }
            return Response.noContent().build();
        } catch (final Throwable t) {
            final StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            final StringBuilder sb = new StringBuilder();
            sb.append("Unexpected exception caught in testConnection(): ");
            sb.append(sw.toString());
            config.setHubUrlError(sb.toString());
            return Response.ok(config).status(Status.BAD_REQUEST).build();
        }
    }

    private HubServerConfigBuilder setConfigBuilderFromSerializableConfig(final HubServerConfigSerializable config,
            final PluginSettings settings) {
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setHubUrl(config.getHubUrl());
        serverConfigBuilder.setTimeout(config.getTimeout());
        serverConfigBuilder.setUsername(config.getUsername());

        if (StringUtils.isBlank(config.getPassword())) {
            serverConfigBuilder.setPassword(null);
            serverConfigBuilder.setPasswordLength(0);
        } else if (StringUtils.isNotBlank(config.getPassword()) && !config.isPasswordMasked()) {
            serverConfigBuilder.setPassword(config.getPassword());
            serverConfigBuilder.setPasswordLength(0);
        } else {
            serverConfigBuilder.setPassword(getValue(settings, HubConfigKeys.CONFIG_HUB_PASS));
            serverConfigBuilder
                    .setPasswordLength(NumberUtils.toInt(getValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH)));
        }

        if (config.getImportSSLCerts() != null) {
            serverConfigBuilder.setAutoImportHttpsCertificates(config.getImportSSLCerts());
        }

        serverConfigBuilder.setProxyHost(config.getHubProxyHost());
        serverConfigBuilder.setProxyPort(config.getHubProxyPort());
        serverConfigBuilder.setIgnoredProxyHosts(config.getHubNoProxyHosts());
        serverConfigBuilder.setProxyUsername(config.getHubProxyUser());

        if (StringUtils.isBlank(config.getHubProxyPassword())) {
            serverConfigBuilder.setProxyPassword(null);
            serverConfigBuilder.setProxyPasswordLength(0);
        } else if (StringUtils.isNotBlank(config.getHubProxyPassword()) && !config.isProxyPasswordMasked()) {
            // only update the stored password if it is not the masked
            // password used for display
            serverConfigBuilder.setProxyPassword(config.getHubProxyPassword());
            serverConfigBuilder.setProxyPasswordLength(0);
        } else {
            serverConfigBuilder.setProxyPassword(getValue(settings, HubConfigKeys.CONFIG_PROXY_PASS));
            serverConfigBuilder.setProxyPasswordLength(
                    NumberUtils.toInt(getValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH)));
        }
        return serverConfigBuilder;
    }

    private void setConfigFromResult(final HubServerConfigSerializable config,
            final AbstractValidator validator) {
        final ValidationResults serverConfigResults = validator.assertValid();
        if (serverConfigResults.hasErrors()) {
            if (serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBURL) != null) {
                config.setHubUrlError(serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBURL));
            }
            if (serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBTIMEOUT) != null) {
                config.setTimeoutError(serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBTIMEOUT));
            }
            if (serverConfigResults.getResultString(HubCredentialsFieldEnum.USERNAME) != null) {
                config.setUsernameError(serverConfigResults.getResultString(HubCredentialsFieldEnum.USERNAME));
            }
            if (serverConfigResults.getResultString(HubCredentialsFieldEnum.PASSWORD) != null) {
                config.setPasswordError(serverConfigResults.getResultString(HubCredentialsFieldEnum.PASSWORD));
            }
            if (serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYHOST) != null) {
                config.setHubProxyHostError(serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYHOST));
            }
            if (serverConfigResults.getResultString(HubProxyInfoFieldEnum.NOPROXYHOSTS) != null) {
                config.setHubNoProxyHostsError(serverConfigResults.getResultString(HubProxyInfoFieldEnum.NOPROXYHOSTS));
            }
            if (serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYPORT) != null) {
                config.setHubProxyPortError(serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYPORT));
            }
            if (serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYUSERNAME) != null) {
                config.setHubProxyUserError(serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYUSERNAME));
            }
            if (serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYPASSWORD) != null) {
                config.setHubProxyPasswordError(serverConfigResults.getResultString(HubProxyInfoFieldEnum.PROXYPASSWORD));
            }
        }
    }

    private String getValue(final PluginSettings settings, final String key) {
        return (String) settings.get(key);
    }

    private void setValue(final PluginSettings settings, final String key, final Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, String.valueOf(value));
        }
    }

}
