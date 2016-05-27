package ut.com.blackducksoftware.integration.hub.bamboo;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.utils.SystemProperty;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

@RunWith(PowerMockRunner.class)
public class HubBambooUtilsPowerMockTest {

	@PrepareForTest(SystemDirectory.class)
	@Test
	public void testGetBambooHome() {
		final File file = new File("bamboo-home/");
		PowerMockito.mockStatic(SystemDirectory.class);
		PowerMockito.when(SystemDirectory.getApplicationHome()).thenReturn(file);

		final String path = HubBambooUtils.getInstance().getBambooHome();

		assertEquals(file.getAbsolutePath(), path);

		PowerMockito.verifyStatic();
		SystemDirectory.getApplicationHome();
	}

	// @PrepareForTest(SystemProperty.class)
	@Test
	public void testGetBambooHomeFromEnv() {
		final File file = new File("bamboo-home/");
		// PowerMockito.mockStatic(SystemProperty.class);
		// PowerMockito.when(SystemProperty.BAMBOO_HOME_FROM_ENV.getValue()).thenReturn(file.getAbsolutePath());
		SystemProperty.BAMBOO_HOME_FROM_ENV.setValue(file.getAbsolutePath());
		final String path = HubBambooUtils.getInstance().getBambooHome();

		assertEquals(file.getAbsolutePath(), path);

		// PowerMockito.verifyStatic();
		assertEquals(file.getAbsolutePath(), SystemProperty.BAMBOO_HOME_FROM_ENV.getValue());
	}
}
