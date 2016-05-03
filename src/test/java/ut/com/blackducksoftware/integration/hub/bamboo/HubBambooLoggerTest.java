package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

import ut.com.blackducksoftware.integration.hub.bamboo.utils.TestBuildLogger;

public class HubBambooLoggerTest {
	private HubBambooLogger logger;
	private TestBuildLogger buildLogger;

	@Before
	public void setUp() {
		buildLogger = new TestBuildLogger();
		logger = new HubBambooLogger(buildLogger);
	}

	@After
	public void tearDown() {
		buildLogger.clearBuildLog();
	}

	@Test
	public void testLogLevel() {
		logger.setLogLevel(LogLevel.TRACE);
		assertEquals(LogLevel.TRACE, logger.getLogLevel());
	}

	@Test
	public void testGetBuildLogger() {
		final BuildLogger tempLogger = logger.getBuildLogger();
		assertEquals(buildLogger, tempLogger);
	}

	@Test
	public void testErrorThrowable() {
		final String message = "TestThrowableMessage";
		final String messagePrefix = "TestPrefix";
		final Throwable t = new Throwable(message);
		logger.setLogLevel(LogLevel.OFF);
		logger.error(t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.setLogLevel(LogLevel.ERROR);

		logger.error(null, t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.error(t);
		assertFalse(buildLogger.getStringErrorLogs().isEmpty());
		assertEquals(2, buildLogger.getStringErrorLogs().size());

		logger.error(messagePrefix, t);
		assertFalse(buildLogger.getStringErrorLogs().isEmpty());
		assertEquals(4, buildLogger.getStringErrorLogs().size());
	}

	@Test
	public void testDebugThrowable() {
		final String message = "TestThrowableMessage";
		final String messagePrefix = "TestPrefix";
		final Throwable t = new Throwable(message);
		logger.setLogLevel(LogLevel.OFF);
		logger.error(t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.setLogLevel(LogLevel.DEBUG);

		logger.debug(null, t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.error(messagePrefix, t);
		assertFalse(buildLogger.getStringErrorLogs().isEmpty());
		assertEquals(2, buildLogger.getStringErrorLogs().size());
	}

	@Test
	public void testTraceThrowable() {
		final String message = "TestThrowableMessage";
		final String messagePrefix = "TestPrefix";
		final Throwable t = new Throwable(message);
		logger.setLogLevel(LogLevel.OFF);
		logger.trace(messagePrefix, t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.setLogLevel(LogLevel.TRACE);

		logger.trace(null, t);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.trace(messagePrefix, t);
		assertFalse(buildLogger.getStringErrorLogs().isEmpty());
		assertEquals(2, buildLogger.getStringErrorLogs().size());
	}

	@Test
	public void testDebug() {
		final String testMessage = "TestMessage";
		logger.setLogLevel(LogLevel.OFF);
		logger.debug(testMessage);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.setLogLevel(LogLevel.DEBUG);

		logger.debug(null);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.debug(testMessage);
		assertFalse(buildLogger.getStringBuildLogs().isEmpty());
	}

	@Test
	public void testError() {
		final String testMessage = "TestMessage";
		logger.setLogLevel(LogLevel.OFF);
		logger.error(testMessage);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.setLogLevel(LogLevel.ERROR);

		final String nullText = null;
		logger.error(nullText);
		assertTrue(buildLogger.getStringErrorLogs().isEmpty());

		logger.error(testMessage);
		assertFalse(buildLogger.getStringErrorLogs().isEmpty());
	}

	@Test
	public void testInfo() {
		final String testMessage = "TestMessage";
		logger.setLogLevel(LogLevel.OFF);
		logger.info(testMessage);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.setLogLevel(LogLevel.INFO);

		logger.info(null);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.info(testMessage);
		assertFalse(buildLogger.getStringBuildLogs().isEmpty());
	}

	@Test
	public void testTrace() {
		final String testMessage = "TestMessage";
		logger.setLogLevel(LogLevel.OFF);
		logger.trace(testMessage);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.setLogLevel(LogLevel.TRACE);

		logger.trace(null);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.trace(testMessage);
		assertFalse(buildLogger.getStringBuildLogs().isEmpty());
	}

	@Test
	public void testWarn() {
		final String testMessage = "TestMessage";
		logger.setLogLevel(LogLevel.OFF);
		logger.warn(testMessage);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.setLogLevel(LogLevel.WARN);

		logger.warn(null);
		assertTrue(buildLogger.getStringBuildLogs().isEmpty());

		logger.warn(testMessage);
		assertFalse(buildLogger.getStringBuildLogs().isEmpty());
	}
}
