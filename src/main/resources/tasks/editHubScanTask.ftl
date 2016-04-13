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