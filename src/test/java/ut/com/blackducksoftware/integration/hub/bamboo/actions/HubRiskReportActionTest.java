package ut.com.blackducksoftware.integration.hub.bamboo.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.bamboo.reports.HubRiskReportAction;
import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubRiskReportActionTest {

	private static final Integer BUILD_NUMBER = new Integer(1);
	private static final String PLAN_KEY = "HUB-TEST-JOB";

	private static HubBambooUtils singleton;
	private static HubRiskReportData reportData;

	@BeforeClass
	public static void captureSingleton() throws Exception {
		singleton = HubBambooUtils.getInstance();

		final HubBambooUtils original = HubBambooUtils.getInstance();
		final HubBambooUtils utilClass = Mockito.mock(HubBambooUtils.class);
		final Field field = HubBambooUtils.class.getDeclaredField("myInstance");
		field.setAccessible(true);
		field.set(original, utilClass);
		final File file = new File(PLAN_KEY + "-" + BUILD_NUMBER.toString() + ".txt");
		file.deleteOnExit();
		file.createNewFile();

		Mockito.when(utilClass.getRiskReportFile(Mockito.anyString(), Mockito.anyInt())).thenReturn(file);

		reportData = createReportData();
		printJSonData(file);
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

	private static void printJSonData(final File file) {

		FileWriter output = null;
		try {

			output = new FileWriter(file);
			final Gson gson = new GsonBuilder().create();
			final String jsonString = gson.toJson(reportData);
			output.write(jsonString);

		} catch (final Exception ex) {

		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (final Exception ex) {

				}
			}
		}
	}

	private static HubRiskReportData createReportData() {
		final HubRiskReportData data = new HubRiskReportData();

		return data;
	}

	@Test
	public void testDoExecute() throws Exception {
		final HubRiskReportAction action = new HubRiskReportAction();
		action.setPlanKey(PLAN_KEY);
		action.setBuildNumber(BUILD_NUMBER);
		final String result = action.doExecute();

		assertEquals(com.opensymphony.xwork2.Action.SUCCESS, result);
		assertNotNull(action.getBundle());
		assertNotNull(action.getHubRiskReportData());
	}
}
