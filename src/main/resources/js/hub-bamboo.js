/*
 * Black Duck Hub Plugin for Bamboo
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var statusMessageFieldId = "aui-hub-message-field";
var statusMessageTitleId = "aui-hub-message-title";
var statusMessageTitleTextId = "aui-hub-message-title-text";
var statusMessageTextId = "aui-hub-message-text";

var errorStatus = "error";
var successStatus = "success";
var hiddenClass = "hidden";

function updateConfig() {
		putConfig(AJS.contextPath() + '/rest/hub-bamboo/1.0/', 'Save successful.', 'The configuration is not valid.');
	}

function testConnection() {
		putConfig(AJS.contextPath() + '/rest/hub-bamboo/1.0/testConnection', 'Test Connection successful.', 'Test Connection failed.');
	}

function putConfig(restUrl, successMessage, failureMessage) {
	console.log("putConfig()");
	  AJS.$.ajax({
		    url: restUrl,
		    type: "PUT",
		    dataType: "json",
		    contentType: "application/json",
		    data: '{ "hubUrl": "' + encodeURI(AJS.$("#hubServerUrl").val())
		    + '", "timeout": "' + encodeURI(AJS.$("#hubTimeout").val())
		    + '", "username": "' + encodeURI(AJS.$("#hubUsername").val())
		    + '", "password": "' + encodeURI(AJS.$("#hubPassword").val())
		    + '", "hubProxyHost": "' + encodeURI(AJS.$("#proxyHost").val())
		    + '", "hubProxyPort": "' + encodeURI(AJS.$("#proxyPort").val())
		    + '", "hubNoProxyHosts": "' + encodeURI(AJS.$("#noProxyHost").val())
		    + '", "hubProxyUser": "' + encodeURI(AJS.$("#proxyUsername").val())
		    + '", "hubProxyPassword": "' + encodeURI(AJS.$("#proxyPassword").val())
		    + '", "importSSLCerts": "' + AJS.$("#importSSLCerts").prop('checked')
		    + '", "hubWorkspaceCheck": "' + AJS.$("#hubWorkspaceCheck").prop('checked')
		    + '"}',
		    processData: false,
		    success: function() {
		    	hideError('hubServerUrlErrorRow', 'hubServerUrlError');
		    	hideError('hubTimeoutErrorRow', 'hubTimeoutError');
		    	hideError('hubUsernameErrorRow', 'hubUsernameError');
		    	hideError('hubPasswordErrorRow', 'hubPasswordError');
		    	hideError('proxyHostErrorRow', 'proxyHostError');
		    	hideError('proxyPortErrorRow', 'proxyPortError');
			    hideError('proxyUsernameErrorRow', 'proxyUsernameError');
			    hideError('proxyPasswordErrorRow', 'proxyPasswordError');
			    hideError('noProxyHostErrorRow', 'noProxyHostError');
			    hideError('configurationErrorRow', 'configurationError');
			      
			    showStatusMessage(successStatus, 'Success!', successMessage);
		    },
		    error: function(response){
		    	console.log("putConfig(): " + response.responseText);
		    	var config = JSON.parse(response.responseText);
		    	handleError('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
			    handleError('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
			    handleError('hubUsernameErrorRow', 'hubUsernameError', config.usernameError);
			    handleError('hubPasswordErrorRow', 'hubPasswordError', config.passwordError);
			    handleError('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
			    handleError('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
			    handleError('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
			    handleError('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
			    handleError('noProxyHostErrorRow', 'noProxyHostError', config.hubNoProxyHostsError);
			    handleError('configurationErrorRow', 'configurationError', config.testConnectionError);
			    
			    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
		    },
		    complete: function(jqXHR, textStatus){
		    	 stopProgressSpinner();
		    }
		  });
}

function populateForm() {
	  AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-bamboo/1.0/",
	    dataType: "json",
	    success: function(config) {
	      updateValue("hubServerUrl", config.hubUrl);
	      updateValue("hubTimeout", config.timeout);
	      updateValue("hubUsername", config.username);
	      updateValue("hubPassword", config.password);
	      updateValue("proxyHost", config.hubProxyHost);
	      updateValue("proxyPort", config.hubProxyPort);
	      updateValue("proxyUsername", config.hubProxyUser);
	      updateValue("proxyPassword", config.hubProxyPassword);
	      updateValue("noProxyHost", config.hubNoProxyHosts);
	      updateCheckBox("importSSLCerts", config.importSSLCerts);
	      updateCheckBox("hubWorkspaceCheck", config.hubWorkspaceCheck);
	      
	      checkProxyConfig();
	      
	      handleError('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
	      handleError('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
	      handleError('hubUsernameErrorRow', 'hubUsernameError', config.usernameError);
	      handleError('hubPasswordErrorRow', 'hubPasswordError', config.passwordError);
	      handleError('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
	      handleError('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
	      handleError('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
	      handleError('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);
	      handleError('noProxyHostErrorRow', 'noProxyHostError', config.hubNoProxyHostsError);
	      handleError('configurationErrorRow', 'configurationError', config.testConnectionError);
			    
	    }, error: function(response){
	    	console.log("putConfig(): " + response.responseText);
	    	alert("There was an error loading the configuration.");
	    	handleDataRetrievalError(response, 'configurationError', "There was a problem retrieving the configuration.", "Configuration Error");
	    },
	    complete: function(jqXHR, textStatus){
	    	 stopProgressSpinner();
	    }
	  });
	}

function handleDataRetrievalError(response, errorId, errorText, dialogTitle){
	var errorField = AJS.$('#' + errorId);
	errorField.text(errorText);
	removeClassFromField(errorField, hiddenClass);
	addClassToField(errorField, "clickable");
	var error = JSON.parse(response.responseText);
	error = AJS.$(error);
	errorField.click(function() {showErrorDialog(dialogTitle, error.attr("message"), error.attr("status-code"), error.attr("stack-trace")) });
}

function showErrorDialog(header, errorMessage, errorCode, stackTrace){
	var errorDialog = new AJS.Dialog({
	    width: 800, 
	    height: 500, 
	    id: 'error-dialog', 
	    closeOnOutsideClick: true
	});

	errorDialog.addHeader(header);
	var errorBody = AJS.$('<div>', {
	});
	var errorMessage = AJS.$('<p>', {
		text : "Error Message : "+ errorMessage
	});
	var errorCode = AJS.$('<p>', {
		text : "Error Code : "+ errorCode
	});
	var errorStackTrace = AJS.$('<p>', {
		text : stackTrace
	});
	errorBody.append(errorMessage, errorCode, errorStackTrace);
	errorDialog.addPanel(header, errorBody, "panel-body");
	errorDialog.addButton("OK", function (dialog) {
		errorDialog.hide();
	});
	errorDialog.show();
}

function checkProxyConfig(){
	var proxyHost = AJS.$("#proxyHost").val();
	var proxyPort = AJS.$("#proxyPort").val();
	var noProxyHost = AJS.$("#noProxyHost").val();
	var proxyUsername = AJS.$("#proxyUsername").val();
	var proxyPassword = AJS.$("#proxyPassword").val();
	
	if(!proxyHost && !proxyPort && !noProxyHost && !proxyUsername && !proxyPassword){
		toggleDisplayById("proxyConfigDisplayIcon",'proxyConfigArea');
	}
}

function updateCheckBox(fieldId, checked) {

	AJS.$("#" + fieldId).prop('checked', checked);
}

function updateValue(fieldId, configField) {
	if(configField){
		 AJS.$("#" + fieldId).val(decodeURI(configField));
    }
}

function handleError(fieldRowId, fieldId, configField) {
	if(configField){
		showError(fieldRowId, fieldId, configField);
    } else{
    	hideError(fieldRowId, fieldId);
    }
}

function showError(fieldRowId, fieldId, configField) {
	  AJS.$("#" + fieldId).text(decodeURI(configField));
  	  removeClassFromFieldById(fieldRowId, hiddenClass);
}

function hideError(fieldRowId, fieldId) {
	  AJS.$("#" + fieldId).text('');
  	  addClassToFieldById(fieldRowId, hiddenClass);
}

function showStatusMessage(status, statusTitle, message) {
	resetStatusMessage();
	if(status == errorStatus){
		addClassToFieldById(statusMessageFieldId, 'error');
		addClassToFieldById(statusMessageTitleId, 'icon-error');
	} else if (status == successStatus){
		addClassToFieldById(statusMessageFieldId, 'success');
		addClassToFieldById(statusMessageTitleId, 'icon-success');
	}
	AJS.$("#" + statusMessageTitleTextId).text(statusTitle);
	AJS.$("#" + statusMessageTextId).text(message);
	removeClassFromFieldById(statusMessageFieldId, hiddenClass);
}

function resetStatusMessage() {
	removeClassFromFieldById(statusMessageFieldId,'error');
	removeClassFromFieldById(statusMessageFieldId,'success');
	removeClassFromFieldById(statusMessageTitleId,'icon-error');
	removeClassFromFieldById(statusMessageTitleId,'icon-success');
	AJS.$("#" + statusMessageTitleTextId).text('');
	AJS.$("#" + statusMessageTextId).text('');
	addClassToFieldById(statusMessageFieldId, hiddenClass);
}

function addClassToFieldById(fieldId, cssClass){
	if(!AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).addClass(cssClass);
	}
}

function removeClassFromFieldById(fieldId, cssClass){
	if(AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).removeClass(cssClass);
	}
}

function addClassToField(field, cssClass){
	if(!AJS.$(field).hasClass(cssClass)){
		AJS.$(field).addClass(cssClass);
	}
}

function removeClassFromField(field, cssClass){
	if(AJS.$(field).hasClass(cssClass)){
		AJS.$(field).removeClass(cssClass);
	}
}

function toggleDisplay(icon, fieldId){
	var iconObject = AJS.$(icon);
	if(iconObject.hasClass('fa-angle-down')){
		removeClassFromField(icon, 'fa-angle-down');
		addClassToField(icon, 'fa-angle-right');
		
		addClassToFieldById(fieldId, hiddenClass);
	} else if(iconObject.hasClass('fa-angle-right')){
		removeClassFromField(icon, 'fa-angle-right');
		addClassToField(icon, 'fa-angle-down');
	
		removeClassFromFieldById(fieldId, hiddenClass);
	}
}

function toggleDisplayById(iconId, fieldId){
	var iconObject = AJS.$('#' + iconId);
	if(iconObject.hasClass('fa-angle-down')){
		removeClassFromFieldById(iconId, 'fa-angle-down');
		addClassToFieldById(iconId, 'fa-angle-right');
		
		addClassToFieldById(fieldId, hiddenClass);
	} else if(iconObject.hasClass('fa-angle-right')){
		removeClassFromFieldById(iconId, 'fa-angle-right');
		addClassToFieldById(iconId, 'fa-angle-down');
	
		removeClassFromFieldById(fieldId, hiddenClass);
	}
}

function startProgressSpinner(){
	 var spinner = AJS.$('#progressSpinner');
	 
	 if(spinner.find("i").length == 0){
		var newSpinnerIcon = AJS.$('<i>', {
		});
		AJS.$(newSpinnerIcon).addClass("largeIcon");
		AJS.$(newSpinnerIcon).addClass("fa");
		AJS.$(newSpinnerIcon).addClass("fa-spinner");
		AJS.$(newSpinnerIcon).addClass("fa-spin");
		AJS.$(newSpinnerIcon).addClass("fa-fw");
		
		newSpinnerIcon.appendTo(spinner);
	 }
}

function stopProgressSpinner(){
	var spinner = AJS.$('#progressSpinner');
	if(spinner.children().length > 0){
		AJS.$(spinner).empty();
	}
}

(function ($) {
	populateForm();
})(AJS.$ || jQuery);

