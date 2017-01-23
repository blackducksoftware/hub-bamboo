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
import java.util.Collection;
import java.util.List;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.atlassian.plugin.predicate.PluginPredicate;

public class PluginAccessorMock implements PluginAccessor {

    private Plugin plugin;

    @Override
    public ClassLoader getClassLoader() {

        return null;
    }

    @Override
    public Iterable<ModuleDescriptor<?>> getDynamicModules(final Plugin arg0) {

        return null;
    }

    @Override
    public Class<?> getDynamicPluginClass(final String arg0) throws ClassNotFoundException {

        return null;
    }

    @Override
    public InputStream getDynamicResourceAsStream(final String arg0) {

        return null;
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> arg0) {

        return null;
    }

    @Override
    public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> arg0, final boolean arg1) {

        return null;
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getEnabledModuleDescriptorsByType(final String arg0) throws PluginParseException {

        return null;
    }

    @Override
    public <M> List<M> getEnabledModulesByClass(final Class<M> arg0) {

        return null;
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>>[] arg0, final Class<M> arg1) {

        return null;
    }

    @Override
    public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>> arg0, final Class<M> arg1) {

        return null;
    }

    @Override
    public Plugin getEnabledPlugin(final String arg0) throws IllegalArgumentException {

        return null;
    }

    @Override
    public ModuleDescriptor<?> getEnabledPluginModule(final String arg0) {

        return null;
    }

    @Override
    public Collection<Plugin> getEnabledPlugins() {

        return null;
    }

    @Override
    public <M> Collection<ModuleDescriptor<M>> getModuleDescriptors(final ModuleDescriptorPredicate<M> arg0) {

        return null;
    }

    @Override
    public <M> Collection<M> getModules(final ModuleDescriptorPredicate<M> arg0) {

        return null;
    }

    @Override
    public Plugin getPlugin(final String name) throws IllegalArgumentException {
        if (plugin != null && plugin.getName().equals(name)) {
            return plugin;
        }
        return null;
    }

    @Override
    public ModuleDescriptor<?> getPluginModule(final String arg0) {

        return null;
    }

    @Override
    public InputStream getPluginResourceAsStream(final String arg0, final String arg1) {

        return null;
    }

    @Override
    public PluginRestartState getPluginRestartState(final String arg0) {

        return null;
    }

    @Override
    public Collection<Plugin> getPlugins() {

        return null;
    }

    @Override
    public Collection<Plugin> getPlugins(final PluginPredicate arg0) {

        return null;
    }

    @Override
    public boolean isPluginEnabled(final String arg0) throws IllegalArgumentException {

        return false;
    }

    @Override
    public boolean isPluginModuleEnabled(final String arg0) {

        return false;
    }

    @Override
    public boolean isSystemPlugin(final String arg0) {

        return false;
    }

    public void setPlugin(final Plugin plugin) {
        this.plugin = plugin;
    }

}
