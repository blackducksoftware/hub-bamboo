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
package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.conditions.RiskReportCondition;

public class RiskReportConditionTest {

	private static final Integer BUILD_NUMBER = new Integer(1);
	private static final String INVALID_FILE_PATH = "a_path_to_nowhere";
	private static final String PLAN_KEY = "HUB-TEST-JOB";

	private static HubBambooUtils singleton;

	@BeforeClass
	public static void captureSingleton() {
		singleton = HubBambooUtils.getInstance();
	}

	@AfterClass
	public static void resetSingleton() {
		try {
			final HubBambooUtils obj = HubBambooUtils.getInstance();

			final Field field = HubBambooUtils.class.getDeclaredField("myInstance");
			field.setAccessible(true);
			field.set(obj, singleton);
		} catch (final Exception ex) {

		}
	}

	@Test
	public void testConditionPlanKeyMissing() throws Exception {
		final RiskReportCondition condition = new RiskReportCondition();
		final Map<String, Object> buildContextMap = new HashMap<String, Object>();
		buildContextMap.put(RiskReportCondition.KEY_BUILD_NUMBER, BUILD_NUMBER);
		final HubBambooUtils original = HubBambooUtils.getInstance();
		final HubBambooUtils utilClass = Mockito.mock(HubBambooUtils.class);
		final Field field = HubBambooUtils.class.getDeclaredField("myInstance");
		field.setAccessible(true);
		field.set(original, utilClass);
		final File file = new File(INVALID_FILE_PATH);
		Mockito.when(utilClass.getRiskReportFile(Mockito.anyString(), Mockito.anyInt())).thenReturn(file);
		assertFalse(condition.shouldDisplay(buildContextMap));
	}

	@Test
	public void testConditionBuildNumberMissing() throws Exception {
		final RiskReportCondition condition = new RiskReportCondition();
		final Map<String, Object> buildContextMap = new HashMap<String, Object>();
		buildContextMap.put(RiskReportCondition.KEY_PLAN_KEY, PLAN_KEY);
		final HubBambooUtils original = HubBambooUtils.getInstance();
		final HubBambooUtils utilClass = Mockito.mock(HubBambooUtils.class);
		final Field field = HubBambooUtils.class.getDeclaredField("myInstance");
		field.setAccessible(true);
		field.set(original, utilClass);
		final File file = new File(INVALID_FILE_PATH);
		Mockito.when(utilClass.getRiskReportFile(Mockito.anyString(), Mockito.anyInt())).thenReturn(file);
		assertFalse(condition.shouldDisplay(buildContextMap));
	}

	@Test
	public void testReportFileExist() throws Exception {
		final RiskReportCondition condition = new RiskReportCondition();
		final Map<String, Object> buildContextMap = new HashMap<String, Object>();
		buildContextMap.put(RiskReportCondition.KEY_PLAN_KEY, PLAN_KEY);
		buildContextMap.put(RiskReportCondition.KEY_BUILD_NUMBER, BUILD_NUMBER);
		final HubBambooUtils original = HubBambooUtils.getInstance();
		final HubBambooUtils utilClass = Mockito.mock(HubBambooUtils.class);
		final Field field = HubBambooUtils.class.getDeclaredField("myInstance");
		field.setAccessible(true);
		field.set(original, utilClass);
		final File file = new File(PLAN_KEY + "-" + BUILD_NUMBER.toString() + ".txt");
		file.deleteOnExit();
		file.createNewFile();
		Mockito.when(utilClass.getRiskReportFile(Mockito.anyString(), Mockito.anyInt())).thenReturn(file);
		final boolean result = condition.shouldDisplay(buildContextMap);
		assertTrue(result);
	}
}
