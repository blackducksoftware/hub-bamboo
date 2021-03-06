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
[@ww.label labelKey='blackduckhub.task.scan.project.label' name='hubProject'/]
[@ww.label labelKey='blackduckhub.task.scan.version.label' name='hubVersion'/]
[@ww.select labelKey='blackduckhub.task.scan.phase.label' name='hubPhase' list='hubPhases' emptyOption='false' readonly='true'/]
[@ww.select labelKey='blackduckhub.task.scan.distribution.label' name='hubDistribution' list='hubDistributions' emptyOption='false' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.projectleveladjustments.label' name='projectLevelAdjustments' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.genriskreport.label' name='shouldGenerateRiskReport' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.failonpolicy.label' name='failOnPolicyViolation' readonly='true'/]
[@ww.textfield labelKey='blackduckhub.task.scan.maxwaittimebomupdate.label' name='maxWaitTimeForBomUpdate'/] 
[@ww.label labelKey='blackduckhub.task.scan.scanmemory.label' name='hubScanMemory'/]
[@ww.label labelKey='blackduckhub.task.scan.codelocationname.label' name='codeLocationAlias' description='This will change the name of the Code Location that is created by this scan. Example Code Location Name could be http://bambooHost:8111-\${bamboo.buildPlanName}'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.dryrun.label' name='dryRun' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.cleanuponsuccessfulscan.label' name='cleanupLogsOnSuccess' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.unmapPreviousCodeLocations.label' name='unmapPreviousCodeLocations' description='Will unmap previous Code Locations mapped to this Project and Version.' readonly='true'/]
[@ww.checkbox labelKey='blackduckhub.task.scan.deletePreviousCodeLocations.label' name='deletePreviousCodeLocations' description='Will delete previous Code Locations mapped to this Project and Version.' readonly='true'/]
[@ww.textarea labelKey='blackduckhub.task.scan.scantargets.label' name='hubTargets' readonly='true' description='Path of the target to be scanned. One target per line.'/]
[@ww.textarea labelKey='blackduckhub.task.scan.excludepatterns.label' name='excludePatterns' readonly='true' description='Excludes a directory from scanning. Leading and trailing slashes are required. One pattern per line.'/]