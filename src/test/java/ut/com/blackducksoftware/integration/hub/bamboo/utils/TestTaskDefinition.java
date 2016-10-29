/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package ut.com.blackducksoftware.integration.hub.bamboo.utils;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.bamboo.core.BambooEntityOid;
import com.atlassian.bamboo.core.BambooEntityType;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRootDirectorySelector;

public class TestTaskDefinition implements TaskDefinition {

    private static final long serialVersionUID = 6572977600672994755L;

    private Map<String, String> configMap = new HashMap<String, String>();

    @Override
    public long getId() {
        return 1;
    }

    @Override
    public String getPluginKey() {

        return null;
    }

    @Override
    public String getUserDescription() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isFinalising() {
        return false;
    }

    @Override
    public Map<String, String> getConfiguration() {
        return configMap;
    }

    @Override
    public TaskRootDirectorySelector getRootDirectorySelector() {
        return null;
    }

    @Override
    public void setConfiguration(final Map<String, String> configMap) {
        this.configMap = configMap;
    }

    @Override
    public void setEnabled(final boolean arg0) {

    }

    @Override
    public void setFinalising(final boolean arg0) {

    }

    @Override
    public void setRootDirectorySelector(final TaskRootDirectorySelector arg0) {

    }

    @Override
    public void setUserDescription(final String arg0) {

    }

    @Override
    public void setOid(BambooEntityOid arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public BambooEntityType getEntityType() {
        // TODO Auto-generated method stub
        return BambooEntityType.JOB;
    }

    @Override
    public BambooEntityOid getOid() {
        // TODO Auto-generated method stub
        return BambooEntityOid.create(getId());
    }
}
