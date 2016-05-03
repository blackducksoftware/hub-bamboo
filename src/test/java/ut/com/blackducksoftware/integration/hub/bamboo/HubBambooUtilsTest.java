package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class HubBambooUtilsTest {

	// private static final String PASSWORD = "password";
	// private static final String USER = "user";
	// private static final String HUB_URL = "https://google.com";
	// private static final String VALID_PORT = "2303";
	// private static final String VALID_HOST = "just need a non-empty string";
	// private static final String VALID_PASSWORD = "itsasecret";
	// private static final String VALID_USERNAME = "memyselfandi";
	// private static final String VALID_IGNORE_HOST_LIST = "google,msn,yahoo";
	// private static final String VALID_IGNORE_HOST = "google";
	// private static final String INVALID_IGNORE_HOST_LIST =
	// "google,[^-z!,abc";
	// private static final String INVALID_IGNORE_HOST = "[^-z!";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	// private final TestLogger logger = new TestLogger();

	@Test
	public void testGetInstance() throws Exception {
		assertNotNull(HubBambooUtils.getInstance());
	}

	// @Test
	// public void testBuildConfig() throws Exception {
	// final HubServerConfig config =
	// HubBambooUtils.getInstance().buildConfigFromStrings(HUB_URL, USER,
	// PASSWORD,
	// VALID_HOST, VALID_PORT, VALID_IGNORE_HOST, VALID_USERNAME,
	// VALID_PASSWORD, logger);
	// assertNotNull(config);
	// assertEquals(HUB_URL, config.getHubUrl());
	// assertEquals(USER, config.getGlobalCredentials().getUsername());
	// assertEquals(PASSWORD,
	// config.getGlobalCredentials().getDecryptedPassword());
	// }
	//
	// @Test
	// public void testBuildProxyInfo() throws Exception {
	// HubProxyInfo info =
	// HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST,
	// VALID_PORT,
	// VALID_IGNORE_HOST, VALID_USERNAME, VALID_PASSWORD, logger);
	// assertNotNull(info);
	// assertEquals(VALID_HOST, info.getHost());
	// assertEquals(VALID_PORT, info.getPort());
	// assertEquals(VALID_IGNORE_HOST, info.getIgnoredProxyHosts());
	// assertEquals(VALID_USERNAME, info.getUsername());
	// assertEquals(VALID_PASSWORD, info.getDecryptedPassword());
	//
	// info = HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST,
	// VALID_PORT, VALID_IGNORE_HOST_LIST,
	// VALID_USERNAME, VALID_PASSWORD, logger);
	// assertNotNull(info);
	// assertEquals(VALID_HOST, info.getHost());
	// assertEquals(VALID_PORT, info.getPort());
	// assertEquals(VALID_IGNORE_HOST_LIST, info.getIgnoredProxyHosts());
	// assertEquals(VALID_USERNAME, info.getUsername());
	// assertEquals(VALID_PASSWORD, info.getDecryptedPassword());
	// }
	//
	// @Test
	// public void testInvalidBuildProxyInfo() throws Exception {
	// exception.expect(HubIntegrationException.class);
	// HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST,
	// VALID_PORT, INVALID_IGNORE_HOST,
	// VALID_USERNAME, VALID_PASSWORD, logger);
	// }
	//
	// @Test
	// public void testInvalidBuildProxyInfoIgnoreList() throws Exception {
	// exception.expect(HubIntegrationException.class);
	// HubBambooUtils.getInstance().buildProxyInfoFromString(VALID_HOST,
	// VALID_PORT, INVALID_IGNORE_HOST_LIST,
	// VALID_USERNAME, VALID_PASSWORD, logger);
	// }
	//
	// @Test
	// public void testInvalidConfig() throws Exception {
	// exception.expect(HubIntegrationException.class);
	// HubBambooUtils.getInstance().buildConfigFromStrings(null, null, null,
	// VALID_HOST, VALID_PORT, VALID_IGNORE_HOST,
	// VALID_USERNAME, VALID_PASSWORD, logger);
	// }

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

	// @Test
	// public void testValidScanTargetPath() throws Exception {
	// String targetText = "aFile\r anotherFile";
	// final File workingDir = new File(".");
	// List<String> targets =
	// HubBambooUtils.getInstance().createScanTargetPaths(targetText,
	// workingDir);
	//
	// assertNotNull(targets);
	// assertEquals(2, targets.size());
	//
	// targetText = "aFile\n anotherFile";
	// targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText,
	// workingDir);
	//
	// assertNotNull(targets);
	// assertEquals(2, targets.size());
	//
	// targetText = "aFile\r\n anotherFile";
	// targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText,
	// workingDir);
	//
	// assertNotNull(targets);
	// assertEquals(2, targets.size());
	// assertEquals(new File(workingDir, "aFile").getAbsolutePath(),
	// targets.get(0));
	// assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(),
	// targets.get(1));
	//
	// targetText = " \r\n anotherFile";
	// targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText,
	// workingDir);
	// assertNotNull(targets);
	// assertEquals(1, targets.size());
	// assertEquals(new File(workingDir, "anotherFile").getAbsolutePath(),
	// targets.get(0));
	//
	// targetText = "aFile\r\n ";
	// targets = HubBambooUtils.getInstance().createScanTargetPaths(targetText,
	// workingDir);
	// assertNotNull(targets);
	// assertEquals(1, targets.size());
	// assertEquals(new File(workingDir, "aFile").getAbsolutePath(),
	// targets.get(0));
	// }
}
