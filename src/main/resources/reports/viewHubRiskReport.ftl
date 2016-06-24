<!-- 
Copyright (C) 2016 Black Duck Software, Inc.
http://www.blackducksoftware.com/

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License.
 -->    
<#assign pluginResourcesPath="${baseUrl}/download/resources/com.blackducksoftware.integration.hub-bamboo:hub-bamboo-resources/"/>
<meta name="decorator" content="result"/>
<meta name="tab" content="hub_risk_report"/>

<link href="${pluginResourcesPath}css/HubBomReport.css" rel="stylesheet" type="text/css" />
<link href="${pluginResourcesPath}font-awesome-4.5.0/css/font-awesome.min.css" rel="stylesheet" type="text/css" />

<div class="riskReportBackgroundColor">
    <div class="reportHeader">
        <div class="h1 reportHeaderTitle">${bundle.getString("title")}</div>

        <div style="float: right;">
            <img class="reportHeaderIcon" src="${pluginResourcesPath}images/Hub_BD_logo.png" />
        </div>
    </div>

    <div class="versionSummaryTable">
        <div>
            <div class="clickable linkText versionSummaryLargeLabel"
                onclick="window.open('${hubRiskReportData.report.reportProjectUrl}', '_blank');">
                ${hubRiskReportData.htmlEscape(hubRiskReportData.report.detailedReleaseSummary.projectName)}</div>
            <div class="versionSummaryLargeLabel">
                <i class="fa fa-caret-right"></i>
            </div>

            <div class="clickable linkText versionSummaryLargeLabel"
                onclick="window.open('${hubRiskReportData.report.reportVersionUrl}', '_blank');">
                ${hubRiskReportData.htmlEscape(hubRiskReportData.report.detailedReleaseSummary.version)}</div>

            <div style="float: right;"
                class="linkText riskReportText clickable evenPadding"
                onclick="window.open('${hubRiskReportData.report.reportVersionUrl}', '_blank');">
                ${bundle.getString("hub.report.link")}</div>
        </div>
        <div>
            <div class="versionSummaryLabel">${bundle.getString("phase")}:</div>
            <div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.report.detailedReleaseSummary.phaseDisplayValue)}</div>
            <div class="versionSummaryLabel">|</div>
            <div class="versionSummaryLabel">${bundle.getString("distribution")}:</div>
            <div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.report.detailedReleaseSummary.distributionDisplayValue)}</div>
        </div>
    </div>

    <!-- SECURITY RISK SUMMARY -->
    <div class="riskSummaryContainer horizontal rounded">
        <div class="riskSummaryContainerLabel">
            ${bundle.getString("vulnerability.risk.title")} <i id="securityDescriptionIcon"
                class="fa fa-info-circle infoIcon"
                title="${bundle.getString('vulnerability.risk.description')}"></i>
        </div>

        <div class="progress-bar horizontal">
            <div id="highSecurityRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.high")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.vulnerabilityRiskHighCount}</div>
            <div class="progress-track">
                <div id="highVulnerabilityRiskBar" class="progress-fill-high">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.vulnerabilityRiskHighCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="mediumSecurityRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.medium")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.vulnerabilityRiskMediumCount}</div>
            <div class="progress-track">
                <div id="mediumVulnerabilityRiskBar" class="progress-fill-medium">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.vulnerabilityRiskMediumCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="lowSecurityRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.low")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.vulnerabilityRiskLowCount}</div>
            <div class="progress-track">
                <div id="lowVulnerabilityRiskBar" class="progress-fill-low">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.vulnerabilityRiskLowCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="noneSecurityRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.none")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.vulnerabilityRiskNoneCount}</div>
            <div class="progress-track">
                <div id="noVulnerabilityRiskBar" class="progress-fill-none">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.vulnerabilityRiskNoneCount)}%</span>
                </div>
            </div>
        </div>
    </div>
    <!-- SECURITY RISK SUMMARY END -->

    <!-- LICENSE  RISK SUMMARY -->
    <div class="riskSummaryContainer horizontal rounded">
        <div class="riskSummaryContainerLabel">
            ${bundle.getString("license.risk.title")} <i id="licenseDescriptionIcon"
                class="fa fa-info-circle infoIcon"
                title="${bundle.getString('license.risk.description')}"></i>
        </div>

        <div class="progress-bar horizontal">
            <div id="highLicenseRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.high")}</div>
            <div class="riskSummaryCount">${hubRiskReportData.licenseRiskHighCount}</div>
            <div class="progress-track">
                <div id="highLicenseRiskBar" class="progress-fill-high">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.licenseRiskHighCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="mediumLicenseRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.medium")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.licenseRiskMediumCount}</div>
            <div class="progress-track">
                <div id="mediumLicenseRiskBar" class="progress-fill-medium">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.licenseRiskMediumCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="lowLicenseRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.low")}</div>
            <div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskLowCount()}</div>
            <div class="progress-track">
                <div id="lowLicenseRiskBar" class="progress-fill-low">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.licenseRiskLowCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="noneLicenseRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.none")}</div>
            <div class="riskSummaryCount">${hubRiskReportData.licenseRiskNoneCount}</div>
            <div class="progress-track">
                <div id="noLicenseRiskBar" class="progress-fill-none">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskNoneCount())}%</span>
                </div>
            </div>
        </div>
    </div>
    <!-- LICENSE RISK SUMMARY END -->

    <!-- OPERATIONAL RISK SUMMARY -->
    <div class="riskSummaryContainer horizontal rounded">
        <div class="riskSummaryContainerLabel">
            ${bundle.getString("operational.risk.title")} <i id="operationalDescriptionIcon"
                class="fa fa-info-circle infoIcon"
                title="${bundle.getString('operational.risk.description')}"></i>
        </div>

        <div class="progress-bar horizontal">
            <div id="highOperationalRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.high")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.operationalRiskHighCount}</div>
            <div class="progress-track">
                <div id="highOperationalRiskBar" class="progress-fill-high">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.operationalRiskHighCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="mediumOperationalRiskLabel"
                class="clickable riskSummaryLabel"
                onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.medium")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.operationalRiskMediumCount}</div>
            <div class="progress-track">
                <div id="mediumOperationalRiskBar" class="progress-fill-medium">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.operationalRiskMediumCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="lowOperationalRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.low")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.operationalRiskLowCount}</div>
            <div class="progress-track">
                <div id="lowOperationalRiskBar" class="progress-fill-low">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.operationalRiskLowCount)}%</span>
                </div>
            </div>
        </div>

        <div class="progress-bar horizontal">
            <div id="noneOperationalRiskLabel" class="clickable riskSummaryLabel"
                onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.none")}</div>
            <div class="riskSummaryCount">
                ${hubRiskReportData.operationalRiskNoneCount}</div>
            <div class="progress-track">
                <div id="noOperationalRiskBar" class="progress-fill-none">
                    <span>${hubRiskReportData.getPercentage(hubRiskReportData.operationalRiskNoneCount)}%</span>
                </div>
            </div>
        </div>
    </div>
    <!-- OPERATIONAL RISK SUMMARY END -->

    <table class="table-summary horizontal">
        <tbody>
            <tr>
                <td class="summaryLabel" style="font-weight: bold;">${bundle.getString("entries")}:</td>
                <td class="summaryValue">${hubRiskReportData.bomEntries.size()}</td>
            </tr>
        </tbody>
    </table>
    <table id="hubBomReport" class="table sortable">
        <thead>
            <tr>
                <th class="clickable componentColumn columnLabel evenPadding">${bundle.getString("component")}</th>
                <th class="clickable componentColumn columnLabel evenPadding">${bundle.getString("version")}</th>
                <th class="clickable columnLabel evenPadding">${bundle.getString("license")}</th>
                <th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.high.short")}</th>
                <th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.medium.short")}</th>
                <th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.low.short")}</th>
                <th class="clickable riskColumnLabel evenPadding"
                    title="${bundle.getString('license.risk.title')}">${bundle.getString("license.risk.title.short")}</th>
                <th class="clickable riskColumnLabel evenPadding"
                    title="${bundle.getString('operational.risk.title')}">${bundle.getString("operational.risk.title.short")}</th>
            </tr>
        </thead>
        <tbody>
            <#list hubRiskReportData.bomEntries as entry>
                <tr>
                    <td class="clickable componentColumn evenPadding"
                        onclick="window.open('${hubRiskReportData.report.getComponentUrl(entry)}', '_blank');">
                        ${hubRiskReportData.htmlEscape(entry.producerProject.name)}</td>
                    <td class="clickable componentColumn evenPadding"
                        onclick="window.open('${hubRiskReportData.report.getVersionUrl(entry)}', '_blank');">
                        ${hubRiskReportData.htmlEscape(entry.producerReleasesDisplay)}</td>
                    <td class="licenseColumn evenPadding"
                        title="${entry.getLicensesDisplay()}">${entry.licensesDisplay}</td>
                    <td class="riskColumn"><div
                            class="risk-span riskColumn risk-count">${entry.vulnerabilityRisk.HIGH}</div></td>
                    <td class="riskColumn"><div
                            class="risk-span riskColumn risk-count">${entry.vulnerabilityRisk.MEDIUM}</div></td>
                    <td class="riskColumn"><div
                            class="risk-span riskColumn risk-count">${entry.vulnerabilityRisk.LOW}</div></td>
                    <td class="riskColumn"><div
                            class="risk-span riskColumn risk-count">${entry.licenseRiskString}</div></td>
                    <td class="riskColumn"><div
                            class="risk-span riskColumn risk-count">${entry.operationalRiskString}</div></td>
                </tr>
            </#list>
        </tbody>
    </table>
</div>
<script type="text/javascript">

    ['${pluginResourcesPath}js/Sortable.js',
     '${pluginResourcesPath}js/HubBomReportFunctions.js',
     '${pluginResourcesPath}js/HubReportStartup.js'
    ].forEach(function (src) {
       var script = document.createElement('script');
       script.type="text/javascript";
       script.src = src; 
       script.async = false;
       document.head.appendChild(script);
    });

</script>  