package com.blackducksoftware.integration.hub.bamboo.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.atlassian.bamboo.build.ViewBuildResults;
import com.atlassian.utils.process.IOUtils;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;
import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.google.gson.Gson;

public class HubRiskReportAction extends ViewBuildResults {

	private static final long serialVersionUID = 4076165272346339757L;

	private HubRiskReportData hubRiskReportData;

	@Override
	public String doExecute() throws Exception {
		createReportData();
		return SUCCESS;
	}

	public HubRiskReportData getHubRiskReportData() {
		return hubRiskReportData;
	}

	private void createReportData() {
		final String planKey = getPlanKey();
		final int buildNumber = getBuildNumber();

		FileReader reader = null;
		try {
			final File fileData = HubBambooUtils.getInstance().getRiskReportFile(planKey, buildNumber);
			reader = new FileReader(fileData);
			final Gson gson = new Gson();
			hubRiskReportData = gson.fromJson(reader, HubRiskReportData.class);

		} catch (final FileNotFoundException e) {
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
}
