package com.blackducksoftware.integration.hub.bamboo.bom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.polling.ScanStatusChecker;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TaskHubEventPolling extends HubEventPolling {

	public TaskHubEventPolling(final HubIntRestService service) {
		super(service);
	}

	/**
	 * Checks the status's in the scan files and polls their URL's, every 10
	 * seconds, until they have all have status COMPLETE. We keep trying until
	 * we hit the maximum wait time. If we find a scan history object that has
	 * status cancelled or an error type then we throw an exception.
	 */
	@Override
	public void assertBomUpToDate(final HubReportGenerationInfo hubReportGenerationInfo, final IntLogger logger)
			throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
		if (StringUtils.isBlank(hubReportGenerationInfo.getScanStatusDirectory())) {
			throw new HubIntegrationException("The scan status directory must be a non empty value.");
		}
		final File statusDirectory = new File(hubReportGenerationInfo.getScanStatusDirectory());

		if (!statusDirectory.exists()) {
			throw new HubIntegrationException("The scan status directory does not exist.");
		}
		if (!statusDirectory.isDirectory()) {
			throw new HubIntegrationException("The scan status directory provided is not a directory.");
		}
		final File[] statusFiles = statusDirectory.listFiles();
		if (statusFiles == null || statusFiles.length == 0) {
			throw new HubIntegrationException("Can not find the scan status files in the directory provided.");
		}
		int expectedNumScans = 0;
		if (hubReportGenerationInfo.getScanTargets() != null && !hubReportGenerationInfo.getScanTargets().isEmpty()) {
			expectedNumScans = hubReportGenerationInfo.getScanTargets().size();
		}
		if (statusFiles.length != expectedNumScans) {
			throw new HubIntegrationException("There were " + expectedNumScans + " scans configured and we found "
					+ statusFiles.length + " status files.");
		}
		logger.info("Checking the directory : " + statusDirectory.getAbsolutePath() + " for the scan status's.");
		final CountDownLatch lock = new CountDownLatch(expectedNumScans);
		final List<ScanStatusChecker> scanStatusList = new ArrayList<ScanStatusChecker>();
		for (final File currentStatusFile : statusFiles) {

			final FileInputStream fileInput = new FileInputStream(currentStatusFile);

			final String fileContent = org.apache.commons.io.IOUtils.toString(fileInput);
			final Gson gson = new GsonBuilder().create();
			final ScanStatusToPoll status = gson.fromJson(fileContent, ScanStatusToPoll.class);
			if (status.get_meta() == null || status.getStatus() == null) {
				throw new HubIntegrationException("The scan status file : " + currentStatusFile.getAbsolutePath()
						+ " does not contain valid scan status json.");
			}
			final ScanStatusChecker checker = new ScanStatusChecker(getService(), status, lock);
			scanStatusList.add(checker);
		}

		logger.debug("Cleaning up the scan status files at : " + statusDirectory.getAbsolutePath());
		// We delete the files in a second loop to ensure we have all the scan
		// status's in memory before we start
		// deleting the files. This way, if there is an exception thrown, the
		// User can go look at the files to see what
		// went wrong.
		for (final File currentStatusFile : statusFiles) {
			currentStatusFile.delete();
		}
		statusDirectory.delete();

		pollScanStatusChecker(lock, hubReportGenerationInfo, scanStatusList);
	}

}
