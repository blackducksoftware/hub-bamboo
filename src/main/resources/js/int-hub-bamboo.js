(function ($) { 
	// form the url 
	var url = AJS.contextPath() + "/rest/blackduckhub-admin/1.0/config";
	
	$(document).ready(function () {
		// request the config information from the server
		$.ajax({
			url: url,
			dataType: "json"
		}).done(function (hubConfig) {
			// populate the form
			// hub settings 
			$("#hubUrl").val(hubConfig.hubUrl);
			$("#hubUser").val(hubConfig.hubUser);
			$("#hubPass").val(hubConfig.hubPass);
			
			// proxy settings
			$("#hubProxyUrl").val(hubConfig.hubProxyUrl);
			$("#hubProxyPort").val(hubConfig.hubProxyPort);
			$("#hubNoProxyHost").val(hubConfig.hubNoProxyHost);
			$("#hubProxyUser").val(hubConfig.hubProxyUser);
			$("#hubProxyPass").val(hubConfig.hubProxyPass);
		});
	});
})(AJS.$ || jQuery);

AJS.$("#hubConfig").submit(function(e) {
	e.preventDefault();
	updateConfig();
});

function updateConfig() {
	AJS.$.ajax({
		url: baseUrl + "/rest/blackduckhub-admin/1.0/config",
		type: "PUT",
		contentType: "application/json",
		data: '{ "hubUrl": "' + AJS.$("#hubUrl".attr("value"))  
			+ '", "hubUser": "' + AJS.$("#hubUser".attr("value"))
			+ '", "hubPass": "' + AJS.$("#hubPass".attr("value"))
			+ '", "hubProxyUrl": "' + AJS.$("#hubProxyUrl".attr("value"))
			+ '", "hubProxyPort": "' + AJS.$("#hubProxyPort".attr("value"))
			+ '", "hubNoProxyHost": "' + AJS.$("#hubNoProxyHost".attr("value"))
			+ '", "hubProxyUser": "' + AJS.$("#hubProxyUser".attr("value"))
			+ '", "hubProxyPass": "' + AJS.$("#hubProxyPass".attr("value")) +'" }',
		processData: false
	});
}