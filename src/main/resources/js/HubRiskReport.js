/**
 * 
 */

var RiskReport = function (jsonData) {
	this.rawdata = jsonData;
};
	RiskReport.prototype.getPercentage = function (count) {
		var totalCount = this.rawdata.totalBomEntries;
        var percentage = 0;
        if (totalCount > 0 && count > 0) {
            percentage = (count / totalCount) * 100;
        }
        return percentage;
	};
	
	RiskReport.prototype.createBaseUrl = function () {
		var detailedReleaseSummary = this.rawdata.report.detailedReleaseSummary; 
		var url = detailedReleaseSummary.uiUrlGenerator.baseUrl;
		return url;
	};
	
	RiskReport.prototype.createProjectUrl = function () {
		var detailedReleaseSummary = this.rawdata.report.detailedReleaseSummary; 
		var url = this.createBaseUrl();
		url = url +"#projects/id:";
        url = url +detailedReleaseSummary.projectId;
		return url;
	};
	
	RiskReport.prototype.createComponentUrl = function (entry) { 
		var url = this.createBaseUrl();
		url = url+"#projects/id:";
		url = url + entry.producerProject.id;
		return url;
	};
	
	RiskReport.prototype.createComponentVersionUrl = function (entry) {
		var url = this.createBaseUrl();
        url = url + "#versions/id:";
        url = url + entry.producerReleases[0].id;
        return url;
	};
	
	RiskReport.prototype.createVersionUrl = function () {
		var detailedReleaseSummary = this.rawdata.report.detailedReleaseSummary; 
		var url = this.createBaseUrl();
		url = url + "#versions/id:";
		url = url + detailedReleaseSummary.versionId;
		url = url + "/view:bom";
		return url;
	};
	
	RiskReport.prototype.createRiskString = function (riskCategory) {
        if (riskCategory.HIGH != 0) {
            return "H";
        } else if (riskCategory.MEDIUM != 0) {
            return "M";
        } else if (riskCategory.LOW != 0) {
            return "L";
        } else {
            return "-";
        }
	};
	
	RiskReport.prototype.createPhaseString = function (phase) {
		if(phase == "PLANNING") {
			return "In Planning";
		} else if (phase == "DEVELOPMENT") {
			return "In Development";
		} else if (phase == "RELEASED") {
			return "Released";
		} else if (phase == "DEPRECATED") {
			return "Deprecated";
		} else if (phase == "ARCHIVED") {
			return "Archived";
		} else {
			return "Unknown Phase";
		}
	};
	
	RiskReport.prototype.createDistributionString = function (distribution) {
		if(distribution == "EXTERNAL") {
			return "External";
		} else if(distribution =="SAAS") {
			return "SaaS";
		} else if(distribution =="INTERNAL") {
			return "Internal";
		} else if(distribution =="OPENSOURCE") { 
			return "Open Source";
		} else { 
			return "Unknown Distribution";
		}
	};
	
	RiskReport.prototype.createHeader = function () {
		var reportHeader = document.createElement("div");
		jQuery(reportHeader).addClass("reportHeader");
		var title = document.createElement("div");
		jQuery(title).addClass("h1 reportHeaderTitle");
		jQuery(title).text("Black Duck Risk Report");
		var icon = document.createElement("div");
		jQuery(icon).addClass("reportHeaderIcon");
		jQuery(icon).css({"float": "right"});
		
		jQuery(reportHeader).append(title);
		jQuery(reportHeader).append(icon);
		return reportHeader;
	};
	
	RiskReport.prototype.createVersionSummary = function () {
		var detailedReleaseSummary = this.rawdata.report.detailedReleaseSummary;
		var table = document.createElement("div");
		jQuery(table).addClass("versionSummaryTable");
		var versionInfo = document.createElement("div");
		var projectName = document.createElement("div");
		var projectVersion = document.createElement("div");
		var moreDetail = document.createElement("div");
		
		jQuery(projectName).addClass("clickable linkText versionSummaryLargeLabel");
		jQuery(projectName).attr("onclick" ,"window.open('"+this.createProjectUrl()+"', '_blank');");
		jQuery(projectName).text(detailedReleaseSummary.projectName);
		
		jQuery(projectVersion).addClass("clickable linkText versionSummaryLargeLabel");
		jQuery(projectVersion).attr("onclick" ,"window.open('"+this.createVersionUrl()+"', '_blank');");
		jQuery(projectVersion).text(detailedReleaseSummary.version);
		
		jQuery(moreDetail).addClass("linkText riskReportText clickable evenPadding");
		jQuery(moreDetail).css({"float": "right"});
		jQuery(moreDetail).attr("onclick" ,"window.open('"+this.createVersionUrl()+"', '_blank');");
		jQuery(moreDetail).text("See more detail...");
		
		jQuery(versionInfo).append(projectName);
		jQuery(versionInfo).append(jQuery('<div class="versionSummaryLargeLabel"><i class="fa fa-caret-right"></i></div>'))
		jQuery(versionInfo).append(projectVersion);
		jQuery(versionInfo).append(moreDetail);
		
		var info = jQuery(document.createElement("div"));
		jQuery(info).append(jQuery('<div class="versionSummaryLabel">Phase:</div>'));
		 jQuery(info).append(jQuery('<div class="versionSummaryLabel">'+this.createPhaseString(detailedReleaseSummary.phase)+'</div>'));
	        jQuery(info).append(jQuery('<div class="versionSummaryLabel">|</div>'));
	        jQuery(info).append(jQuery('<div class="versionSummaryLabel">Distribution:</div>'));
	        jQuery(info).append(jQuery('<div class="versionSummaryLabel">'+this.createDistributionString(detailedReleaseSummary.distribution)+'</div>'));		
	   jQuery(table).append(versionInfo);
       jQuery(table).append(info);	
	        
	   return table;
	};
	
	RiskReport.prototype.createHorizontalBar = function (labelId,labelValue, clickFnName, barId,barValue,barStyleClass) {
		  var percentage = this.getPercentage(barValue)+'%';
	      return  jQuery('<div class="progress-bar horizontal">'
             +'<div id="'+labelId +'" class="clickable riskSummaryLabel"'
             +    'onclick="'+clickFnName+'(this)">' +labelValue+'</div>'
             +'<div class="riskSummaryCount">' +barValue+'</div>'
             +'<div class="progress-track">'
             +'    <div id="'+barId+'" class="'+barStyleClass+'" style="width:'+percentage+'">'
             +'        <span style="display:none;">'+percentage+'</span>'
             +'    </div>'
             +'</div>'
         +'</div>');
	};
	
	RiskReport.prototype.createSecurityRiskContainer = function () {
		var container = document.createElement("div");
		jQuery(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		jQuery(labelDiv).addClass("riskSummaryContainerLabel");
		jQuery(labelDiv).text("Security Risk").append('<i id="securityDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk on number of component versions based on known vulnerabilities."></i>');		
		jQuery(container).append(labelDiv);
		
		jQuery(container).append(this.createHorizontalBar('highSecurityRiskLabel','High','filterTableByVulnerabilityRisk','highVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskHighCount,'progress-fill-high'));
		jQuery(container).append(this.createHorizontalBar('mediumSecurityRiskLabel','Medium','filterTableByVulnerabilityRisk','mediumVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskMediumCount,'progress-fill-medium'));
		jQuery(container).append(this.createHorizontalBar('lowSecurityRiskLabel','Low','filterTableByVulnerabilityRisk','lowVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskLowCount,'progress-fill-low'));
		jQuery(container).append(this.createHorizontalBar('noneSecurityRiskLabel','None','filterTableByVulnerabilityRisk','noneVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createLicenseRiskContainer = function () {
		var container = document.createElement("div");
		jQuery(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		jQuery(labelDiv).addClass("riskSummaryContainerLabel");
		jQuery(labelDiv).text("License Risk").append('<i id="licenseDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk based on open source software (OSS) license use in your projects."></i>');		
		jQuery(container).append(labelDiv);
		
		jQuery(container).append(this.createHorizontalBar('highLicenseRiskLabel','High','filterTableByLicenseRisk','highLicenseRiskBar',this.rawdata.licenseRiskHighCount,'progress-fill-high'));
		jQuery(container).append(this.createHorizontalBar('mediumLicenseRiskLabel','Medium','filterTableByLicenseRisk','mediumLicenseRiskBar',this.rawdata.licenseRiskMediumCount,'progress-fill-medium'));
		jQuery(container).append(this.createHorizontalBar('lowLicenseRiskLabel','Low','filterTableByLicenseRisk','lowLicenseRiskBar',this.rawdata.licenseRiskLowCount,'progress-fill-low'));
		jQuery(container).append(this.createHorizontalBar('noneLicenseRiskLabel','None','filterTableByLicenseRisk','noneLicenseRiskBar',this.rawdata.licenseRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createOperationalRiskContainer = function () {
		var container = document.createElement("div");
		jQuery(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		jQuery(labelDiv).addClass("riskSummaryContainerLabel");
		jQuery(labelDiv).text("Operational Risk").append('<i id="securityDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk based on tracking overall open source software (OSS) component activity."></i>');		
		jQuery(container).append(labelDiv);
		
		jQuery(container).append(this.createHorizontalBar('highOperationalRiskLabel','High','filterTableByOperationalRisk','highOperationalRiskBar',this.rawdata.operationalRiskHighCount,'progress-fill-high'));
		jQuery(container).append(this.createHorizontalBar('mediumOperationalRiskLabel','Medium','filterTableByOperationalRisk','mediumOperationalRiskBar',this.rawdata.operationalRiskMediumCount,'progress-fill-medium'));
		jQuery(container).append(this.createHorizontalBar('lowOperationalRiskLabel','Low','filterTableByOperationalRisk','lowOperationalRiskBar',this.rawdata.operationalRiskLowCount,'progress-fill-low'));
		jQuery(container).append(this.createHorizontalBar('noneOperationalRiskLabel','None','filterTableByOperationalRisk','noneOperationalRiskBar',this.rawdata.operationalRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createSummaryTable = function () {
		var table = document.createElement("table");
		jQuery(table).addClass("table-summary horizontal")
		var tableBody = document.createElement("tbody");
		var tableRow = document.createElement("tr");
		var tableDataLabel = document.createElement("td");
		jQuery(tableDataLabel).addClass("summaryLabel");
		jQuery(tableDataLabel).css({"font-weight" : "bold"});
		jQuery(tableDataLabel).text("BOM Entries");
		var tableDataValue = document.createElement("td");
		jQuery(tableDataValue).addClass("summaryLabel");
		jQuery(tableDataValue).text(this.rawdata.totalBomEntries);
		
		jQuery(tableRow).append(tableDataLabel);
		jQuery(tableRow).append(tableDataValue);
		jQuery(tableBody).append(tableRow);
		jQuery(table).append(tableBody);
		
		return table;
	};
	
	RiskReport.prototype.columnClickEvent = function () {
		if(this.initSortTable == false) {
			console.log("init sorttable");
			sorttable.makeSortable(document.getElementById('hubBomReport'));
		} else {
			console.log("sortable table inited");
		}
	};
	
	RiskReport.prototype.createComponentTableHead = function () {
		var compStyleClass = "clickable componentColumn columnLabel evenPadding";
		var licenseStyleClass = "clickable columnLabel evenPadding";
		var riskStyleClass = "clickable riskColumnLabel evenPadding";
		
		var tableHead = document.createElement("thead");
		var tableHeadRow = document.createElement("tr");
		jQuery(tableHeadRow).append(document.createElement("th"));
		
		var columnHeadComponent = document.createElement("th");
		jQuery(columnHeadComponent).addClass(compStyleClass);
		jQuery(columnHeadComponent).text("Component");
		
		var columnHeadVersion = document.createElement("th");
		jQuery(columnHeadVersion).addClass(compStyleClass);
		jQuery(columnHeadVersion).text("Version");
		
		var columnHeadLicense = document.createElement("th");
		jQuery(columnHeadLicense).addClass(licenseStyleClass);
		jQuery(columnHeadLicense).text("License");
		
		var columnHeadEntryHigh = document.createElement("th");
		jQuery(columnHeadEntryHigh).addClass(riskStyleClass);
		jQuery(columnHeadEntryHigh).text("H");
		
		var columnHeadEntryMedium = document.createElement("th");
		jQuery(columnHeadEntryMedium).addClass(riskStyleClass);
		jQuery(columnHeadEntryMedium).text("M");
		
		var columnHeadEntryLow = document.createElement("th");
		jQuery(columnHeadEntryLow).addClass(riskStyleClass);
		jQuery(columnHeadEntryLow).text("L");
		
		var columnHeadLicenseRisk = document.createElement("th");
		jQuery(columnHeadLicenseRisk).addClass(riskStyleClass);
		jQuery(columnHeadLicenseRisk).attr("title","License Risk");
		jQuery(columnHeadLicenseRisk).text("Lic R");
		
		var columnHeadOperationRisk = document.createElement("th");
		jQuery(columnHeadOperationRisk).addClass(riskStyleClass);
		jQuery(columnHeadOperationRisk).attr("title","Operational Risk");
		jQuery(columnHeadOperationRisk).text("Opt R");
		
		jQuery(tableHeadRow).append(columnHeadComponent);
		jQuery(tableHeadRow).append(columnHeadVersion);
		jQuery(tableHeadRow).append(columnHeadLicense);
		jQuery(tableHeadRow).append(columnHeadEntryHigh);
		jQuery(tableHeadRow).append(columnHeadEntryMedium);
		jQuery(tableHeadRow).append(columnHeadEntryLow);
		jQuery(tableHeadRow).append(columnHeadLicenseRisk);
		jQuery(tableHeadRow).append(columnHeadOperationRisk);
		
		jQuery(tableHead).append(tableHeadRow);
		return tableHead;
	};
	
	RiskReport.prototype.createComponentTableRow = function (entry) {
		var tableRow = document.createElement("tr");
		var columnApprovalStatus = document.createElement("td");
		jQuery(columnApprovalStatus).addClass("evenPadding violation");
		jQuery(columnApprovalStatus).append(jQuery('<i class="fa fa-ban"></i>'));
		var approvalDiv = document.createElement("div");
		jQuery(approvalDiv).text(entry.policyApprovalStatus);
		jQuery(columnApprovalStatus).append(approvalDiv);
	
		var columnComponent = document.createElement("td");
		jQuery(columnComponent).addClass("clickable componentColumn evenPadding");
		jQuery(columnComponent).attr("onclick" ,"window.open('"+this.createComponentUrl(entry)+"', '_blank');");
		jQuery(columnComponent).text(entry.producerProject.name);

        var columnVersion = document.createElement("td");
        jQuery(columnVersion).addClass("clickable componentColumn evenPadding");
		jQuery(columnVersion).attr("onclick" ,"window.open('"+this.createComponentVersionUrl(entry)+"', '_blank');");
		jQuery(columnVersion).text(entry.producerReleases[0].version);
        
        var columnLicense = document.createElement("td");
        jQuery(columnLicense).addClass("licenseColumn evenPadding");
        jQuery(columnLicense).attr("title",entry.licenses[0].licenseDisplay);
        jQuery(columnLicense).text(entry.licenses[0].licenseDisplay);
        
        var riskCategories = entry.riskProfile.categories;
        var vulnerabilityRiskProfile = riskCategories.VULNERABILITY;
        
        var columnHighRisk = document.createElement("td");
        jQuery(columnHighRisk).addClass("riskColumn");
        var highRiskDiv = document.createElement("div");
        jQuery(highRiskDiv).addClass("risk-span riskColumn risk-count");
        jQuery(highRiskDiv).text(vulnerabilityRiskProfile.HIGH);
        jQuery(columnHighRisk).append(highRiskDiv);

        var columnMediumRisk = document.createElement("td");
        jQuery(columnMediumRisk).addClass("riskColumn");
        var mediumRiskDiv = document.createElement("div");
        jQuery(mediumRiskDiv).addClass("risk-span riskColumn risk-count");
        jQuery( mediumRiskDiv).text(vulnerabilityRiskProfile.MEDIUM);
        jQuery(columnMediumRisk).append(mediumRiskDiv);

        var columnLowRisk = document.createElement("td");
        jQuery(columnLowRisk).addClass("riskColumn");
        var lowRiskDiv = document.createElement("div");
        jQuery(lowRiskDiv).addClass("risk-span riskColumn risk-count");
        jQuery(lowRiskDiv).text(vulnerabilityRiskProfile.LOW);
        jQuery(columnLowRisk).append(lowRiskDiv);

        var columnLicenseRisk = document.createElement("td");
        jQuery(columnLicenseRisk).addClass("riskColumn");
        var licRiskDiv = document.createElement("div");
        jQuery(licRiskDiv).addClass("risk-span riskColumn risk-count");
        jQuery(licRiskDiv).text(this.createRiskString(riskCategories.LICENSE));
        jQuery(columnLicenseRisk).append(licRiskDiv);
        
        var columnOperationalRisk = document.createElement("td");
                
        jQuery(columnOperationalRisk).addClass("riskColumn");
        var opRiskDiv = document.createElement("div");
        jQuery(opRiskDiv).addClass("risk-span riskColumn risk-count");
        jQuery(opRiskDiv).text(this.createRiskString(riskCategories.OPERATIONAL));
        jQuery(columnOperationalRisk).append(opRiskDiv);
        
        jQuery(tableRow).append(columnApprovalStatus);
        jQuery(tableRow).append(columnComponent);
        jQuery(tableRow).append(columnVersion);
        jQuery(tableRow).append(columnLicense);
        jQuery(tableRow).append(columnHighRisk);
        jQuery(tableRow).append(columnMediumRisk);
        jQuery(tableRow).append(columnLowRisk);
        jQuery(tableRow).append(columnLicenseRisk);
        jQuery(tableRow).append(columnOperationalRisk);
        
		return tableRow;
	};
	
	RiskReport.prototype.createComponentTable = function () {
		var table = document.createElement("table");
		jQuery(table).attr("id","hubBomReport");
		jQuery(table).addClass("table sortable");
		jQuery(table).attr("onmouseenter","initSortTable();");
		
		jQuery(table).append(this.createComponentTableHead());
		var tableBody = document.createElement("tbody");
		jQuery(tableBody).attr("id","hubBomReportBody");
		var entryArray = this.rawdata.report.aggregateBomViewEntries;
		var index;
		var odd = true;
		for (index in entryArray) {
			var tableRow = this.createComponentTableRow(entryArray[index]);
			adjustTableRow(tableRow, odd);
			adjustSecurityRisks(tableRow);
			adjustOtherRisks(tableRow, licenseRiskColumnNum);
			adjustOtherRisks(tableRow, operationRiskColumnNum);
			odd = !odd;
			jQuery(tableBody).append(tableRow);
		}
		jQuery(table).append(tableBody);
		return table;
	};
	
	RiskReport.prototype.createReport = function () {
		var report = document.createElement("div")
		jQuery(report).addClass("riskReportBackgroundColor");
		jQuery(report).append(this.createHeader());
		jQuery(report).append(this.createVersionSummary());
		jQuery(report).append(this.createSecurityRiskContainer());
		jQuery(report).append(this.createLicenseRiskContainer());
		jQuery(report).append(this.createOperationalRiskContainer());
		jQuery(report).append(this.createSummaryTable());
		var table = this.createComponentTable();
		jQuery(report).append(table);
		jQuery("#riskReportDiv").html(jQuery(report).html());
	};