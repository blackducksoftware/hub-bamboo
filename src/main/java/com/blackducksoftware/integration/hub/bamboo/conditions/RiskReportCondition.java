package com.blackducksoftware.integration.hub.bamboo.conditions;

import java.util.Collection;
import java.util.Map;

import com.atlassian.bamboo.build.artifact.ArtifactLink;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;

public class RiskReportCondition extends AbstractCompositeCondition {

	private static final String KEY_BUILD_NUMBER = "buildNumber";
	private static final String KEY_PLAN_KEY = "planKey";
	private ResultsSummaryManager resultsSummaryManager;

	@Override
	public boolean shouldDisplay(final Map<String, Object> buildContextMap) {

		final boolean display = false;
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

			final ResultsSummary resultSummary = resultsSummaryManager.getResultsSummary(resultKey);
			final Collection<ArtifactLink> artifacts = resultSummary.getArtifactLinks();

			for (final ArtifactLink link : artifacts) {

			}

		}

		return display;
	}

	public void setResultsSummaryManager(final ResultsSummaryManager resultsSummaryManager) {
		this.resultsSummaryManager = resultsSummaryManager;
	}
}
