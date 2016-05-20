package com.blackducksoftware.integration.hub.bamboo.conditions;

import java.io.File;
import java.util.Map;

import com.atlassian.bamboo.build.artifact.ArtifactLinkManager;
import com.atlassian.bamboo.fileserver.ArtifactStorage;
import com.atlassian.bamboo.fileserver.SystemDirectory;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.bamboo.security.SecureTokenService;
import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class RiskReportCondition extends AbstractCompositeCondition {

	private static final String KEY_BUILD_NUMBER = "buildNumber";
	private static final String KEY_PLAN_KEY = "planKey";
	private ResultsSummaryManager resultsSummaryManager;
	private ArtifactLinkManager linkManager;
	private SecureTokenService secureTokenService;

	@Override
	public boolean shouldDisplay(final Map<String, Object> buildContextMap) {

		boolean display = false;
		if (resultsSummaryManager != null) {
			String planKey = "";
			int buildNumber = -1;

			if (buildContextMap.containsKey(KEY_PLAN_KEY)) {
				planKey = buildContextMap.get(KEY_PLAN_KEY).toString();
			}

			if (buildContextMap.containsKey(KEY_BUILD_NUMBER)) {
				buildNumber = Integer.valueOf(buildContextMap.get(KEY_BUILD_NUMBER).toString()).intValue();
			}

			final PlanResultKey resultKey = PlanKeys.getPlanResultKey(planKey, buildNumber);

			try {
				// TODO Have to test this on another server SystemDirectory may
				// fail on remote Bamboo server.
				final ArtifactStorage storage = SystemDirectory.getArtifactStorage();

				final File planRoot = storage.getArtifactDirectory(resultKey);
				final File riskReportRoot = new File(planRoot, HubBambooUtils.HUB_RISK_REPORT_ARTIFACT_NAME);
				final File dataFile = new File(riskReportRoot, HubBambooUtils.HUB_RISK_REPORT_FILENAME);
				display = dataFile.exists();
			} catch (final Throwable ex) {
				ex.printStackTrace();
			}
		}

		return display;
	}

	public void setResultsSummaryManager(final ResultsSummaryManager resultsSummaryManager) {
		this.resultsSummaryManager = resultsSummaryManager;
	}

	public void setArtifactLinkManager(final ArtifactLinkManager linkManager) {
		this.linkManager = linkManager;
	}

	public void setSecureTokenService(final SecureTokenService secureTokenService) {
		this.secureTokenService = secureTokenService;
	}
}
