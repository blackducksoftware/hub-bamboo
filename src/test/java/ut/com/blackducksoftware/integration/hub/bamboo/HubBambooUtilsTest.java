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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;

import ut.com.blackducksoftware.integration.hub.bamboo.utils.TestLogger;

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

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private final TestLogger logger = new TestLogger();

	@Test
	public void testGetInstance() throws Exception {
		assertNotNull(HubBambooUtils.getInstance());
	}

	@Test
	public void testBuildConfig() throws Exception {
		final HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				VALID_HOST, VALID_PORT, VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, logger);
		assertNotNull(config);
		assertEquals(new URL(HUB_URL), config.getHubUrl());
		assertEquals(USER, config.getGlobalCredentials().getUsername());
		assertEquals(PASSWORD, config.getGlobalCredentials().getDecryptedPassword());
	}

	@Test
	public void testBuildProxyInfo() throws Exception {
		HubProxyInfo info = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT,
				VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, logger);
		assertNotNull(info);
		assertEquals(VALID_HOST, info.getHost());
		assertEquals(Integer.valueOf(VALID_PORT).intValue(), info.getPort());
		assertEquals(VALID_IGNORE_HOST, info.getIgnoredProxyHosts());
		assertEquals(VALID_USERNAME, info.getUsername());
		assertEquals(VALID_PASSWORD, info.getDecryptedPassword());

		info = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, VALID_IGNORE_HOST_LIST,
				VALID_USERNAME, VALID_PASSWORD, logger);
		assertNotNull(info);
		assertEquals(VALID_HOST, info.getHost());
		assertEquals(Integer.valueOf(VALID_PORT).intValue(), info.getPort());
		assertEquals(VALID_IGNORE_HOST_LIST, info.getIgnoredProxyHosts());
		assertEquals(VALID_USERNAME, info.getUsername());
		assertEquals(VALID_PASSWORD, info.getDecryptedPassword());
	}

	@Test
	public void testInvalidBuildProxyInfo() throws Exception {
		exception.expect(HubIntegrationException.class);
		HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, INVALID_IGNORE_HOST,
				VALID_USERNAME, VALID_PASSWORD, logger);
	}

	@Test
	public void testInvalidBuildProxyInfoIgnoreList() throws Exception {
		exception.expect(HubIntegrationException.class);
		HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST, VALID_PORT, INVALID_IGNORE_HOST_LIST,
				VALID_USERNAME, VALID_PASSWORD, logger);
	}

	@Test
	public void testInvalidConfig() throws Exception {
		exception.expect(HubIntegrationException.class);
		HubBambooUtils.getInstance().buildConfigFromStrings(null, null, null, VALID_HOST, VALID_PORT, VALID_IGNORE_HOST,
				VALID_USERNAME, VALID_PASSWORD, logger);
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
				null, null, null, null, null, logger);
		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceProxyForUrl() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);

		HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				VALID_HOST, VALID_PORT, null, null, null, logger);
		HubBambooUtils.getInstance().configureProxyToService(config, service);

		config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD, VALID_HOST, "0", null,
				null, null, logger);
		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceProxyConfig() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);
		final HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				VALID_HOST, VALID_PORT, VALID_IGNORE_HOST, null, null, logger);
		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}

	@Test
	public void testConfigureServiceAuthenticatedProxyConfig() throws Exception {
		final HubIntRestService service = new HubIntRestService(HUB_URL);
		final HubServerConfig config = HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER, PASSWORD,
				VALID_HOST, VALID_PORT, VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, logger);
		HubBambooUtils.getInstance().configureProxyToService(config, service);
	}
}
