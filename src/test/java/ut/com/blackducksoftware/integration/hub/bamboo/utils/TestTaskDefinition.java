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

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskRootDirectorySelector;

public class TestTaskDefinition implements TaskDefinition {
	private static final long serialVersionUID = 6572977600672994755L;

	private Map<String, String> configMap = new HashMap<String, String>();

	public long getId() {
		return 1;
	}

	public String getPluginKey() {

		return null;
	}

	public String getUserDescription() {
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isFinalising() {
		return false;
	}

	public Map<String, String> getConfiguration() {
		return configMap;
	}

	public TaskRootDirectorySelector getRootDirectorySelector() {
		return null;
	}

	public void setConfiguration(final Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public void setEnabled(final boolean arg0) {

	}

	public void setFinalising(final boolean arg0) {

	}

	public void setRootDirectorySelector(final TaskRootDirectorySelector arg0) {

	}

	public void setUserDescription(final String arg0) {

	}
}
