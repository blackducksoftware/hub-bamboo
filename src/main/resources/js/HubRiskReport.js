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
		AJS.$(reportHeader).addClass("reportHeader");
		var title = document.createElement("div");
		AJS.$(title).addClass("h1 reportHeaderTitle");
		AJS.$(title).text("Black Duck Risk Report");
		var icon = document.createElement("div");
		AJS.$(icon).addClass("reportHeaderIcon");
		AJS.$(icon).css({"float": "right"});
		
		AJS.$(reportHeader).append(title);
		AJS.$(reportHeader).append(icon);
		return reportHeader;
	};
	
	RiskReport.prototype.createVersionSummary = function () {
		var detailedReleaseSummary = this.rawdata.report.detailedReleaseSummary;
		var table = document.createElement("div");
		AJS.$(table).addClass("versionSummaryTable");
		var versionInfo = document.createElement("div");
		var projectName = document.createElement("div");
		var projectVersion = document.createElement("div");
		var moreDetail = document.createElement("div");
		
		AJS.$(projectName).addClass("clickable linkText versionSummaryLargeLabel");
		AJS.$(projectName).attr("onclick" ,"window.open('"+this.createProjectUrl()+"', '_blank');");
		AJS.$(projectName).text(detailedReleaseSummary.projectName);
		
		AJS.$(projectVersion).addClass("clickable linkText versionSummaryLargeLabel");
		AJS.$(projectVersion).attr("onclick" ,"window.open('"+this.createVersionUrl()+"', '_blank');");
		AJS.$(projectVersion).text(detailedReleaseSummary.version);
		
		AJS.$(moreDetail).addClass("linkText riskReportText clickable evenPadding");
		AJS.$(moreDetail).css({"float": "right"});
		AJS.$(moreDetail).attr("onclick" ,"window.open('"+this.createVersionUrl()+"', '_blank');");
		AJS.$(moreDetail).text("See more detail...");
		
		AJS.$(versionInfo).append(projectName);
		AJS.$(versionInfo).append(AJS.$('<div class="versionSummaryLargeLabel"><i class="fa fa-caret-right"></i></div>'))
		AJS.$(versionInfo).append(projectVersion);
		AJS.$(versionInfo).append(moreDetail);
		
		var info = AJS.$(document.createElement("div"));
		AJS.$(info).append(AJS.$('<div class="versionSummaryLabel">Phase:</div>'));
		 AJS.$(info).append(AJS.$('<div class="versionSummaryLabel">'+this.createPhaseString(detailedReleaseSummary.phase)+'</div>'));
	        AJS.$(info).append(AJS.$('<div class="versionSummaryLabel">|</div>'));
	        AJS.$(info).append(AJS.$('<div class="versionSummaryLabel">Distribution:</div>'));
	        AJS.$(info).append(AJS.$('<div class="versionSummaryLabel">'+this.createDistributionString(detailedReleaseSummary.distribution)+'</div>'));		
	   AJS.$(table).append(versionInfo);
       AJS.$(table).append(info);	
	        
	   return table;
	};
	
	RiskReport.prototype.createHorizontalBar = function (labelId,labelValue, clickFnName, barId,barValue,barStyleClass) {
		  var percentage = this.getPercentage(barValue)+'%';
	      return  AJS.$('<div class="progress-bar horizontal">'
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
		AJS.$(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		AJS.$(labelDiv).addClass("riskSummaryContainerLabel");
		AJS.$(labelDiv).text("Security Risk").append('<i id="securityDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk on number of component versions based on known vulnerabilities."></i>');		
		AJS.$(container).append(labelDiv);
		
		AJS.$(container).append(this.createHorizontalBar('highSecurityRiskLabel','High','filterTableByVulnerabilityRisk','highVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskHighCount,'progress-fill-high'));
		AJS.$(container).append(this.createHorizontalBar('mediumSecurityRiskLabel','Medium','filterTableByVulnerabilityRisk','mediumVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskMediumCount,'progress-fill-medium'));
		AJS.$(container).append(this.createHorizontalBar('lowSecurityRiskLabel','Low','filterTableByVulnerabilityRisk','lowVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskLowCount,'progress-fill-low'));
		AJS.$(container).append(this.createHorizontalBar('noneSecurityRiskLabel','None','filterTableByVulnerabilityRisk','noneVulnerabilityRiskBar',this.rawdata.vulnerabilityRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createLicenseRiskContainer = function () {
		var container = document.createElement("div");
		AJS.$(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		AJS.$(labelDiv).addClass("riskSummaryContainerLabel");
		AJS.$(labelDiv).text("License Risk").append('<i id="licenseDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk based on open source software (OSS) license use in your projects."></i>');		
		AJS.$(container).append(labelDiv);
		
		AJS.$(container).append(this.createHorizontalBar('highLicenseRiskLabel','High','filterTableByLicenseRisk','highLicenseRiskBar',this.rawdata.licenseRiskHighCount,'progress-fill-high'));
		AJS.$(container).append(this.createHorizontalBar('mediumLicenseRiskLabel','Medium','filterTableByLicenseRisk','mediumLicenseRiskBar',this.rawdata.licenseRiskMediumCount,'progress-fill-medium'));
		AJS.$(container).append(this.createHorizontalBar('lowLicenseRiskLabel','Low','filterTableByLicenseRisk','lowLicenseRiskBar',this.rawdata.licenseRiskLowCount,'progress-fill-low'));
		AJS.$(container).append(this.createHorizontalBar('noneLicenseRiskLabel','None','filterTableByLicenseRisk','noneLicenseRiskBar',this.rawdata.licenseRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createOperationalRiskContainer = function () {
		var container = document.createElement("div");
		AJS.$(container).addClass("riskSummaryContainer horizontal rounded");
		var labelDiv = document.createElement("div");
		AJS.$(labelDiv).addClass("riskSummaryContainerLabel");
		AJS.$(labelDiv).text("Operational Risk").append('<i id="securityDescriptionIcon"'
	                                              +'class="fa fa-info-circle infoIcon"'
	                                              +'title="Calculated risk based on tracking overall open source software (OSS) component activity."></i>');		
		AJS.$(container).append(labelDiv);
		
		AJS.$(container).append(this.createHorizontalBar('highOperationalRiskLabel','High','filterTableByOperationalRisk','highOperationalRiskBar',this.rawdata.operationalRiskHighCount,'progress-fill-high'));
		AJS.$(container).append(this.createHorizontalBar('mediumOperationalRiskLabel','Medium','filterTableByOperationalRisk','mediumOperationalRiskBar',this.rawdata.operationalRiskMediumCount,'progress-fill-medium'));
		AJS.$(container).append(this.createHorizontalBar('lowOperationalRiskLabel','Low','filterTableByOperationalRisk','lowOperationalRiskBar',this.rawdata.operationalRiskLowCount,'progress-fill-low'));
		AJS.$(container).append(this.createHorizontalBar('noneOperationalRiskLabel','None','filterTableByOperationalRisk','noneOperationalRiskBar',this.rawdata.operationalRiskNoneCount,'progress-fill-none'));
		return container;
	};
	
	RiskReport.prototype.createSummaryTable = function () {
		var table = document.createElement("table");
		AJS.$(table).addClass("table-summary horizontal")
		var tableBody = document.createElement("tbody");
		var tableRow = document.createElement("tr");
		var tableDataLabel = document.createElement("td");
		AJS.$(tableDataLabel).addClass("summaryLabel");
		AJS.$(tableDataLabel).css({"font-weight" : "bold"});
		AJS.$(tableDataLabel).text("BOM Entries");
		var tableDataValue = document.createElement("td");
		AJS.$(tableDataValue).addClass("summaryLabel");
		AJS.$(tableDataValue).text(this.rawdata.totalBomEntries);
		
		AJS.$(tableRow).append(tableDataLabel);
		AJS.$(tableRow).append(tableDataValue);
		AJS.$(tableBody).append(tableRow);
		AJS.$(table).append(tableBody);
		
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
		AJS.$(tableHeadRow).append(document.createElement("th"));
		
		var columnHeadComponent = document.createElement("th");
		AJS.$(columnHeadComponent).addClass(compStyleClass);
		AJS.$(columnHeadComponent).text("Component");
		
		var columnHeadVersion = document.createElement("th");
		AJS.$(columnHeadVersion).addClass(compStyleClass);
		AJS.$(columnHeadVersion).text("Version");
		
		var columnHeadLicense = document.createElement("th");
		AJS.$(columnHeadLicense).addClass(licenseStyleClass);
		AJS.$(columnHeadLicense).text("License");
		
		var columnHeadEntryHigh = document.createElement("th");
		AJS.$(columnHeadEntryHigh).addClass(riskStyleClass);
		AJS.$(columnHeadEntryHigh).text("H");
		
		var columnHeadEntryMedium = document.createElement("th");
		AJS.$(columnHeadEntryMedium).addClass(riskStyleClass);
		AJS.$(columnHeadEntryMedium).text("M");
		
		var columnHeadEntryLow = document.createElement("th");
		AJS.$(columnHeadEntryLow).addClass(riskStyleClass);
		AJS.$(columnHeadEntryLow).text("L");
		
		var columnHeadLicenseRisk = document.createElement("th");
		AJS.$(columnHeadLicenseRisk).addClass(riskStyleClass);
		AJS.$(columnHeadLicenseRisk).attr("title","License Risk");
		AJS.$(columnHeadLicenseRisk).text("Lic R");
		
		var columnHeadOperationRisk = document.createElement("th");
		AJS.$(columnHeadOperationRisk).addClass(riskStyleClass);
		AJS.$(columnHeadOperationRisk).attr("title","Operational Risk");
		AJS.$(columnHeadOperationRisk).text("Opt R");
		
		AJS.$(tableHeadRow).append(columnHeadComponent);
		AJS.$(tableHeadRow).append(columnHeadVersion);
		AJS.$(tableHeadRow).append(columnHeadLicense);
		AJS.$(tableHeadRow).append(columnHeadEntryHigh);
		AJS.$(tableHeadRow).append(columnHeadEntryMedium);
		AJS.$(tableHeadRow).append(columnHeadEntryLow);
		AJS.$(tableHeadRow).append(columnHeadLicenseRisk);
		AJS.$(tableHeadRow).append(columnHeadOperationRisk);
		
		AJS.$(tableHead).append(tableHeadRow);
		return tableHead;
	};
	
	RiskReport.prototype.createComponentTableRow = function (entry) {
		var tableRow = document.createElement("tr");
		var columnApprovalStatus = document.createElement("td");
		AJS.$(columnApprovalStatus).addClass("evenPadding violation");
		AJS.$(columnApprovalStatus).append(AJS.$('<i class="fa fa-ban"></i>'));
		var approvalDiv = document.createElement("div");
		AJS.$(approvalDiv).text(entry.policyApprovalStatus);
		AJS.$(columnApprovalStatus).append(approvalDiv);
	
		var columnComponent = document.createElement("td");
		AJS.$(columnComponent).addClass("clickable componentColumn evenPadding");
		AJS.$(columnComponent).attr("onclick" ,"window.open('"+this.createComponentUrl(entry)+"', '_blank');");
		AJS.$(columnComponent).text(entry.producerProject.name);

        var columnVersion = document.createElement("td");
        AJS.$(columnVersion).addClass("clickable componentColumn evenPadding");
		AJS.$(columnVersion).attr("onclick" ,"window.open('"+this.createComponentVersionUrl(entry)+"', '_blank');");
		AJS.$(columnVersion).text(entry.producerReleases[0].version);
        
        var columnLicense = document.createElement("td");
        AJS.$(columnLicense).addClass("licenseColumn evenPadding");
        AJS.$(columnLicense).attr("title",entry.licenses[0].licenseDisplay);
        AJS.$(columnLicense).text(entry.licenses[0].licenseDisplay);
        
        var riskCategories = entry.riskProfile.categories;
        var vulnerabilityRiskProfile = riskCategories.VULNERABILITY;
        
        var columnHighRisk = document.createElement("td");
        AJS.$(columnHighRisk).addClass("riskColumn");
        var highRiskDiv = document.createElement("div");
        AJS.$(highRiskDiv).addClass("risk-span riskColumn risk-count");
        AJS.$(highRiskDiv).text(vulnerabilityRiskProfile.HIGH);
        AJS.$(columnHighRisk).append(highRiskDiv);

        var columnMediumRisk = document.createElement("td");
        AJS.$(columnMediumRisk).addClass("riskColumn");
        var mediumRiskDiv = document.createElement("div");
        AJS.$(mediumRiskDiv).addClass("risk-span riskColumn risk-count");
        AJS.$( mediumRiskDiv).text(vulnerabilityRiskProfile.MEDIUM);
        AJS.$(columnMediumRisk).append(mediumRiskDiv);

        var columnLowRisk = document.createElement("td");
        AJS.$(columnLowRisk).addClass("riskColumn");
        var lowRiskDiv = document.createElement("div");
        AJS.$(lowRiskDiv).addClass("risk-span riskColumn risk-count");
        AJS.$(lowRiskDiv).text(vulnerabilityRiskProfile.LOW);
        AJS.$(columnLowRisk).append(lowRiskDiv);

        var columnLicenseRisk = document.createElement("td");
        AJS.$(columnLicenseRisk).addClass("riskColumn");
        var licRiskDiv = document.createElement("div");
        AJS.$(licRiskDiv).addClass("risk-span riskColumn risk-count");
        AJS.$(licRiskDiv).text(this.createRiskString(riskCategories.LICENSE));
        AJS.$(columnLicenseRisk).append(licRiskDiv);
        
        var columnOperationalRisk = document.createElement("td");
                
        AJS.$(columnOperationalRisk).addClass("riskColumn");
        var opRiskDiv = document.createElement("div");
        AJS.$(opRiskDiv).addClass("risk-span riskColumn risk-count");
        AJS.$(opRiskDiv).text(this.createRiskString(riskCategories.OPERATIONAL));
        AJS.$(columnOperationalRisk).append(opRiskDiv);
        
        AJS.$(tableRow).append(columnApprovalStatus);
        AJS.$(tableRow).append(columnComponent);
        AJS.$(tableRow).append(columnVersion);
        AJS.$(tableRow).append(columnLicense);
        AJS.$(tableRow).append(columnHighRisk);
        AJS.$(tableRow).append(columnMediumRisk);
        AJS.$(tableRow).append(columnLowRisk);
        AJS.$(tableRow).append(columnLicenseRisk);
        AJS.$(tableRow).append(columnOperationalRisk);
        
		return tableRow;
	};
	
	RiskReport.prototype.createComponentTable = function () {
		var table = document.createElement("table");
		AJS.$(table).attr("id","hubBomReport");
		AJS.$(table).addClass("table sortable");
		AJS.$(table).attr("onmouseenter","initSortTable();");
		
		AJS.$(table).append(this.createComponentTableHead());
		var tableBody = document.createElement("tbody");
		AJS.$(tableBody).attr("id","hubBomReportBody");
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
			AJS.$(tableBody).append(tableRow);
		}
		AJS.$(table).append(tableBody);
		return table;
	};
	
	RiskReport.prototype.createReport = function () {
		var report = document.createElement("div")
		AJS.$(report).addClass("riskReportBackgroundColor");
		AJS.$(report).append(this.createHeader());
		AJS.$(report).append(this.createVersionSummary());
		AJS.$(report).append(this.createSecurityRiskContainer());
		AJS.$(report).append(this.createLicenseRiskContainer());
		AJS.$(report).append(this.createOperationalRiskContainer());
		AJS.$(report).append(this.createSummaryTable());
		var table = this.createComponentTable();
		AJS.$(report).append(table);
		AJS.$("#riskReportDiv").html(AJS.$(report).html());
	};