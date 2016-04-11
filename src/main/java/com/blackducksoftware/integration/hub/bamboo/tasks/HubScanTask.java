package com.blackducksoftware.integration.hub.bamboo.tasks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.bamboo.configuration.ConfigurationMap;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubScanJobConfig;
import com.blackducksoftware.integration.hub.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.bamboo.HubBambooLogger;
import com.blackducksoftware.integration.hub.bamboo.config.ConfigManager;
import com.blackducksoftware.integration.hub.bamboo.config.HubConfig;
import com.blackducksoftware.integration.hub.bamboo.config.HubServiceUtils;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class HubScanTask implements TaskType {

	private ConfigManager configManager;

	public TaskResult execute(final TaskContext taskContext) throws TaskException {

		final TaskResultBuilder resultBuilder = TaskResultBuilder.newBuilder(taskContext).failed();
		final HubBambooLogger logger = new HubBambooLogger(taskContext.getBuildLogger());
		try {

			final ConfigurationMap configurationMap = taskContext.getConfigurationMap();
			final String project = configurationMap.get(HubScanParamEnum.PROJECT.getKey());
			final String version = configurationMap.get(HubScanParamEnum.PROJECT.getKey());
			final String phase = configurationMap.get(HubScanParamEnum.PROJECT.getKey());
			final String distribution = configurationMap.get(HubScanParamEnum.PROJECT.getKey());
			final boolean generateRiskReport = false;
			final int maxWaitTimeForRiskReport = 5;
			final int scanMemory = 4096;
			final String targets = configurationMap.get(HubScanParamEnum.PROJECT.getKey());

			final List<String> scanTargets = createScanTargetPaths(targets);

			final HubConfig hubConfig = configManager.readConfig();
			final HubIntRestService service = getService(hubConfig);

			final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder();
			hubScanJobConfigBuilder.setProjectName(project);
			hubScanJobConfigBuilder.setVersion(version);
			hubScanJobConfigBuilder.setPhase(phase);
			hubScanJobConfigBuilder.setDistribution(distribution);
			hubScanJobConfigBuilder.setWorkingDirectory("/");
			hubScanJobConfigBuilder.setShouldGenerateRiskReport(false);
			// hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(5);
			hubScanJobConfigBuilder.setScanMemory(scanMemory);
			hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargets);
			hubScanJobConfigBuilder.disableScanTargetPathExistenceCheck();

			final HubScanJobConfig jobConfig = hubScanJobConfigBuilder.build(logger);

			service.setCookies(hubConfig.getHubUser(), hubConfig.getHubPass());
		} catch (final HubIntegrationException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final URISyntaxException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final BDRestException e) {
			logger.error("Hub Scan Task error", e);
		} catch (final IOException e) {
			logger.error("Hub Scan Task error", e);
		}

		resultBuilder.success();

		return resultBuilder.build();
	}

	private List<String> createScanTargetPaths(final String targetPathText) {

		final List<String> scanTargets = new ArrayList<String>();

		return scanTargets;

	}

	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;
	}

	private HubIntRestService getService(final HubConfig hubConfig) {
		final HubIntRestService service = new HubIntRestService(hubConfig.getHubUrl());
		HubServiceUtils.getInstance().configureProxyToService(hubConfig, service);
		return service;
	}
}
