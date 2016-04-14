<!-- 
 Copyright (C) 2016 Black Duck Software, Inc.

 http://www.blackducksoftware.com/

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License version 2 only
 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License version 2
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 -->
[@ww.textfield labelKey='blackduckhub.task.scan.project.label' name='hubProject'/]
[@ww.textfield labelKey='blackduckhub.task.scan.version.label' name='hubVersion'/]
[@ww.combobox labelKey='blackduckhub.task.scan.phase.label' name='hubPhase' 
              list="{'In Planning','In Development','Released','Deprecated','Archived'}" emptyOption='false' value='In Planning'/]
[@ww.combobox labelKey='blackduckhub.task.scan.distribution.label' name='hubDistribution'
              list="{'External','SaaS','Internal','Open Source'}" emptyOption='false' value='External'/]
<!--
[@ww.checkbox labelKey='blackduckhub.task.scan.genriskreport.label' name='generateRiskReport'/]
-->
[@ww.checkbox labelKey='blackduckhub.task.scan.failonpolicy.label' name='failOnPolicyViolation'/]
[@ww.textfield labelKey='blackduckhub.task.scan.maxwaittimebomupdate.label' name='maxWaitTimeForBomUpdate'/] 
[@ww.textfield labelKey='blackduckhub.task.scan.scanmemory.label' name='hubScanMemory' required='true'/]
[@ww.textarea labelKey='blackduckhub.task.scan.scantargets.label' name='hubTargets'/]