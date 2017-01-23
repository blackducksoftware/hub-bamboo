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
[@ww.textfield labelKey='blackduckhub.task.scan.project.label' name='hubProject'/]
[@ww.textfield labelKey='blackduckhub.task.scan.version.label' name='hubVersion'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.genriskreport.label' name='shouldGenerateRiskReport'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.failonpolicy.label' name='failOnPolicyViolation'/]
[@ww.textfield labelKey='blackduckhub.task.scan.maxwaittimebomupdate.label' name='maxWaitTimeForBomUpdate'/] 
[@ww.textfield labelKey='blackduckhub.task.scan.scanmemory.label' name='hubScanMemory' required='true'/]
[@ww.textfield labelKey='blackduckhub.task.scan.codelocationname.label' name='codeLocationAlias' description='This will change the name of the Code Location that is created by this scan. Example Code Location Name could be $\{bamboo.buildPlanName}'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.dryrun.label' name='dryRun'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.cleanuponsuccessfulscan.label' name='cleanupLogsOnSuccess'/]
[@ww.textarea labelKey='blackduckhub.task.scan.scantargets.label' name='hubTargets' description='Path of the target to be scanned. One target per line.'/]
[@ww.textarea labelKey='blackduckhub.task.scan.excludepatterns.label' name='excludePatterns' description='Excludes a directory from scanning. Leading and trailing slashes are required. One pattern per line.'/]