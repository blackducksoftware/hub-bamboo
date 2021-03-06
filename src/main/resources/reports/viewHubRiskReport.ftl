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
<html>
	<head>
		<meta name="decorator" content="result"/>
		<meta name="tab" content="hub_risk_report"/>
		${webResourceManager.requireResource("com.blackducksoftware.integration.hub-bamboo:hub-bamboo-resources")}
	</head>
	<iframe id="reportFrame" width="100%"></iframe>
    <script type="text/javascript">
      // TODO refactor this code into an object loader for the JSON look into the URL JQuery plugin.
      //  http://stackoverflow.com/questions/19491336/get-url-parameter-jquery-or-how-to-get-query-string-values-in-js
      var getUrlParameter = function getUrlParameter(sParam) {
            var sPageURL = decodeURIComponent(window.location.search.substring(1)),
                sURLVariables = sPageURL.split('&'),
                sParameterName,
                i;
        
            for (i = 0; i < sURLVariables.length; i++) {
                sParameterName = sURLVariables[i].split('=');
        
                if (sParameterName[0] === sParam) {
                    return sParameterName[1] === undefined ? true : sParameterName[1];
                }
            }
        };
      function getRiskReportHtml() {
        var url = window.location.href;
        var urlSplit = url.split("plugins");
        url = urlSplit[0];
        var planKey = getUrlParameter("planKey");
        var buildNumber = getUrlParameter("buildNumber");
        var lastIndex = planKey.lastIndexOf("-");
        var projectPlan = planKey.substring(0,lastIndex);
        var job = planKey.substring(lastIndex+1);
        var artifactUrl = url+'artifact/' + projectPlan + '/' + job +'/build-' + buildNumber + '/Hub_Risk_Report/riskreport.html';
        console.log("Risk Report html file "+artifactUrl);
        return artifactUrl;
      }
     
     var frame = document.getElementById("reportFrame");
     frame.onload = function() {
       setTimeout(function () {
       var frame = document.getElementById("reportFrame");
        frame.height = frame.contentWindow.document.body.scrollHeight;
        }, 200);
     };
     frame.src = getRiskReportHtml();
    </script>
</html>