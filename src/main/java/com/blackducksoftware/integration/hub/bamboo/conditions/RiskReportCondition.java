package com.blackducksoftware.integration.hub.bamboo.conditions;

import java.io.File;
import java.util.Map;

import com.atlassian.plugin.web.conditions.AbstractCompositeCondition;
import com.blackducksoftware.integration.hub.bamboo.HubBambooUtils;

public class RiskReportCondition extends AbstractCompositeCondition {

	private static final String KEY_BUILD_NUMBER = "buildNumber";
	private static final String KEY_PLAN_KEY = "planKey";

	@Override
	public boolean shouldDisplay(final Map<String, Object> buildContextMap) {

		boolean display = false;
		String planKey = "";
		int buildNumber = -1;

		try {
			if (buildContextMap.containsKey(KEY_PLAN_KEY)) {
				planKey = buildContextMap.get(KEY_PLAN_KEY).toString();
			}

			if (buildContextMap.containsKey(KEY_BUILD_NUMBER)) {
				buildNumber = Integer.valueOf(buildContextMap.get(KEY_BUILD_NUMBER).toString()).intValue();
			}
			final File dataFile = HubBambooUtils.getInstance().getRiskReportFile(planKey, buildNumber);

			if (dataFile != null) {
				display = dataFile.exists();
			}
		} catch (final Throwable t) {
			// nothing to do display will be false
		}

		return display;
	}
}
