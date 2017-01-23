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
package com.blackducksoftware.integration.atlassian.mocks;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.atlassian.plugin.InstallationMode;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.PluginDependencies;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;

public class PluginMock implements Plugin {

    private final String name;

    public PluginMock(final String name) {
        this.name = name;
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(final String arg0, final String arg1) {

        return null;
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors() {

        return null;
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(final String arg0) {

        return null;
    }

    @Override
    public ResourceLocation getResourceLocation(final String arg0, final String arg1) {

        return null;
    }

    @Override
    public int compareTo(final Plugin o) {

        return 0;
    }

    @Override
    public void addModuleDescriptor(final ModuleDescriptor<?> arg0) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean containsSystemModule() {

        return false;
    }

    @Override
    public void disable() throws PluginException {

    }

    @Override
    public void enable() throws PluginException {

    }

    @Override
    public Set<String> getActivePermissions() {

        return null;
    }

    @Override
    public ClassLoader getClassLoader() {

        return null;
    }

    @Override
    public Date getDateEnabled() {

        return null;
    }

    @Override
    public Date getDateEnabling() {

        return null;
    }

    @Override
    public Date getDateInstalled() {

        return null;
    }

    @Override
    public Date getDateLoaded() {

        return null;
    }

    @Override
    public PluginDependencies getDependencies() {

        return null;
    }

    @Override
    public String getI18nNameKey() {

        return null;
    }

    @Override
    public InstallationMode getInstallationMode() {

        return null;
    }

    @Override
    public String getKey() {

        return null;
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(final String arg0) {

        return null;
    }

    @Override
    public Collection<ModuleDescriptor<?>> getModuleDescriptors() {

        return null;
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(final Class<M> arg0) {

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public PluginArtifact getPluginArtifact() {

        return null;
    }

    @Override
    public PluginInformation getPluginInformation() {

        return null;
    }

    @Override
    public PluginState getPluginState() {

        return null;
    }

    @Override
    public int getPluginsVersion() {

        return 0;
    }

    @Override
    public Set<String> getRequiredPlugins() {

        return null;
    }

    @Override
    public URL getResource(final String arg0) {

        return null;
    }

    @Override
    public InputStream getResourceAsStream(final String arg0) {

        return null;
    }

    @Override
    public boolean hasAllPermissions() {

        return false;
    }

    @Override
    public void install() throws PluginException {

    }

    @Override
    public boolean isBundledPlugin() {

        return false;
    }

    @Override
    public boolean isDeleteable() {

        return false;
    }

    @Override
    public boolean isDynamicallyLoaded() {

        return false;
    }

    @Override
    public boolean isEnabled() {

        return false;
    }

    @Override
    public boolean isEnabledByDefault() {

        return false;
    }

    @Override
    public boolean isSystemPlugin() {

        return false;
    }

    @Override
    public boolean isUninstallable() {

        return false;
    }

    @Override
    public <T> Class<T> loadClass(final String arg0, final Class<?> arg1) throws ClassNotFoundException {

        return null;
    }

    @Override
    public void resolve() {

    }

    @Override
    public void setEnabled(final boolean arg0) {

    }

    @Override
    public void setEnabledByDefault(final boolean arg0) {

    }

    @Override
    public void setI18nNameKey(final String arg0) {

    }

    @Override
    public void setKey(final String arg0) {

    }

    @Override
    public void setName(final String name) {
    }

    @Override
    public void setPluginInformation(final PluginInformation arg0) {

    }

    @Override
    public void setPluginsVersion(final int arg0) {

    }

    @Override
    public void setResources(final Resourced arg0) {

    }

    @Override
    public void setSystemPlugin(final boolean arg0) {

    }

    @Override
    public void uninstall() throws PluginException {

    }

}
