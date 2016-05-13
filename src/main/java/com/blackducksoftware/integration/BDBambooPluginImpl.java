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
package com.blackducksoftware.integration;

import com.atlassian.sal.api.ApplicationProperties;

public class BDBambooPluginImpl implements BDBambooPlugin {
	private final ApplicationProperties applicationProperties;

	public BDBambooPluginImpl(final ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	public String getName() {
		if (null != applicationProperties) {
			return "BDBambooPlugin:" + applicationProperties.getDisplayName();
		}

		return "BDBambooPlugin";
	}
}