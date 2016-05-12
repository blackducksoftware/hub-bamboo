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
package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.builder.HubCredentialsBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;

public class HubBambooUtilsTest {

	private static final String PASSWORD = "password";
	private static final String USER = "user";
	private static final String HUB_URL = "https://google.com";
	private static final String VALID_PORT = "2303";
	private static final String VALID_HOST = "http://yahoo.com";
	private static final String VALID_PASSWORD = "itsasecret";
	private static final String VALID_USERNAME = "memyselfandi";
	private static final String VALID_IGNORE_HOST_LIST = "google,msn,yahoo";
	private static final String VALID_IGNORE_HOST = "google";
	private static final String INVALID_IGNORE_HOST_LIST = "google,[^-z!,abc";
	private static final String INVALID_IGNORE_HOST = "[^-z!";
	private static final String EMPTY_PASSWORD_LENGTH = "";

	private final Map<String, String> map1 = new HashMap<String, String>();
	private final Map<String, String> map2 = new HashMap<String, String>();

	private static final String key_1 = "key1";
	private static final String key_2 = "key2";
	private static final String key_3 = "key3";
	private static final String key_4 = "key4";
	private static final String value_1 = "value1";
	private static final String value_2 = "value2";
	private static final String value_3 = "value3";
	private static final String value_4 = "value4";

	private Map<String, String> createVarMap() {
		map1.put(key_1, value_1);
		map1.put(key_2, value_2);
		map2.put(key_3, value_3);
		map2.put(key_4, value_4);

		final Map<String, String> result = HubBambooUtils.getInstance().getEnvironmentVariablesMap(map1, map2);
		return result;
	}

	@Test
	public void testGetInstance() throws Exception {
		assertNotNull(HubBambooUtils.getInstance());
	}

	@Test
	public void testBuildConfig() throws Exception {
		final HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				EMPTY_PASSWORD_LENGTH, VALID_HOST, VALID_PORT, VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD,
				EMPTY_PASSWORD_LENGTH);
		assertNotNull(config);
		assertEquals(new URL(HUB_URL), config.getHubUrl());
		assertEquals(USER, config.getGlobalCredentials().getUsername());
		assertEquals(PASSWORD, config.getGlobalCredentials().getDecryptedPassword());
	}

	@Test
	public void testBuildProxyInfo() throws Exception {
		HubProxyInfo info = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD);
		assertNotNull(info);
		assertEquals(VALID_HOST, info.getHost());
		assertEquals(Integer.valueOf(VALID_PORT).intValue(), info.getPort());
		assertEquals(VALID_IGNORE_HOST, info.getIgnoredProxyHosts());
		assertEquals(VALID_USERNAME, info.getUsername());
		assertEquals(VALID_PASSWORD, info.getDecryptedPassword());

		info = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, VALID_IGNORE_HOST_LIST,
				VALID_USERNAME, VALID_PASSWORD);
		assertNotNull(info);
		assertEquals(VALID_HOST, info.getHost());
		assertEquals(Integer.valueOf(VALID_PORT).intValue(), info.getPort());
		assertEquals(VALID_IGNORE_HOST_LIST, info.getIgnoredProxyHosts());
		assertEquals(VALID_USERNAME, info.getUsername());
		assertEquals(VALID_PASSWORD, info.getDecryptedPassword());
	}

	@Test
	public void testInvalidBuildProxyInfo() throws Exception {
		HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, INVALID_IGNORE_HOST,
				VALID_USERNAME, VALID_PASSWORD);
	}

	@Test
	public void testInvalidBuildProxyInfoIgnoreList() throws Exception {
		HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, INVALID_IGNORE_HOST_LIST,
				VALID_USERNAME, VALID_PASSWORD);
	}

	@Test
	public void testInvalidConfig() throws Exception {
		HubBambooUtils.getInstance().buildConfigFromStrings(null, null, null, null, VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, EMPTY_PASSWORD_LENGTH);
	}

	@Test
	public void testValidScanTargetList() throws Exception {
		final String targetText = "aFile";
		final File workingDir = new File(".");
		List<String> targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, targetText).getAbsolutePath(), targets.get(0));

		targets = HubBambooUtils.getInstance().createScanTargetPaths("", workingDir);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());

		targets = HubBambooUtils.getInstance().createScanTargetPaths(null, workingDir);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());
	}

	@Test
	public void testValidScanTargetPath() throws Exception {
		String targetText = "aFile\r\nanotherFile";
		final File workingDir = new File(".");
		List<String> targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(2, targets.size());

		targetText = "aFile\nanotherFile";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);

		assertNotNull(targets);
		assertEquals(2, targets.size());
		assertEquals(new File(workingDir, "aFile").getAbsolutePath(), targets.get(0));
		assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(), targets.get(1));

		targetText = " \nanotherFile";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(), targets.get(0));

		targetText = "aFile\n ";
		targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText, workingDir);
		assertNotNull(targets);
		assertEquals(1, targets.size());
		assertEquals(new File(workingDir, "aFile").getAbsolutePath(), targets.get(0));
	}

	@Test
	public void testConfigureServiceNullProxy() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);
		final HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				EMPTY_PASSWORD_LENGTH, null, null, null, null, null, null);
		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceProxyForUrl() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);

		final HubProxyInfo proxyInfo = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, null, null);
		final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
		credBuilder.setUsername(VALID_USERNAME);
		credBuilder.setPassword(VALID_PASSWORD);
		final HubCredentials creds = credBuilder.build().getConstructedObject();
		final HubServerConfig config = new HubServerConfig(new URL(VALID_HOST), 5, creds, proxyInfo);

		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceProxyConfig() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);
		final HubProxyInfo proxyInfo = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, null, null);
		final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
		credBuilder.setUsername(VALID_USERNAME);
		credBuilder.setPassword(VALID_PASSWORD);
		final HubCredentials creds = credBuilder.build().getConstructedObject();
		final HubServerConfig config = new HubServerConfig(new URL(VALID_HOST), 5, creds, proxyInfo);

		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceAuthenticatedProxyConfig() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);
		final HubProxyInfo proxyInfo = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, VALID_USERNAME, VALID_PORT);
		final HubCredentialsBuilder credBuilder = new HubCredentialsBuilder();
		credBuilder.setUsername(VALID_USERNAME);
		credBuilder.setPassword(VALID_PASSWORD);
		final HubCredentials creds = credBuilder.build().getConstructedObject();
		final HubServerConfig config = new HubServerConfig(new URL(VALID_HOST), 5, creds, proxyInfo);

		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testCreateEnvVarMap() {

		final Map<String, String> result = createVarMap();
		assertEquals(result.size(), map1.size() + map2.size());
		assertEquals(result.get(key_1), value_1);
		assertEquals(result.get(key_2), value_2);
		assertEquals(result.get(key_3), value_3);
		assertEquals(result.get(key_4), value_4);
		assertFalse(result.containsKey("anunknownkey"));
	}

	@Test
	public void testGetVariable() {
		final Map<String, String> result = createVarMap();
		result.put("bamboo_" + key_1, value_1);
		result.put("bamboo_" + key_2, value_2);
		result.put("bamboo_" + key_3, value_3);
		result.put("bamboo_" + key_4, value_4);

		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_1, false), value_1);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_2, false), value_2);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_3, false), value_3);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_4, false), value_4);

		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_1, true), value_1);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_2, true), value_2);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_3, true), value_3);
		assertEquals(HubBambooUtils.getInstance().getEnvironmentVariable(result, key_4, true), value_4);

		assertNull(HubBambooUtils.getInstance().getEnvironmentVariable(result, "anunknownkey", false));
		assertNull(HubBambooUtils.getInstance().getEnvironmentVariable(result, "anunknownkey", true));
	}
}
