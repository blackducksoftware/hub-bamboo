[@ww.label labelKey='blackduckhub.task.scan.project.label' name='hubProject'/]
[@ww.label labelKey='blackduckhub.task.scan.version.label' name='hubVersion'/]
[@ww.combobox labelKey='blackduckhub.task.scan.phase.label' name='hubPhase' 
              list="{'In Planning','In Development','Released','Deprecated','Archived'}" emptyOption='false' readonly='true'/]
[@ww.combobox labelKey='blackduckhub.task.scan.distribution.label' name='hubDistribution'
              list="{'External','SaaS','Internal','Open Source'}" emptyOption='false' readonly='true'/]
<!--
[@ww.label labelKey='blackduckhub.task.scan.genriskreport.label' name='generateRiskReport'/]
[@ww.label labelKey='blackduckhub.task.scan.maxriskreportwaittime.label' name='maxWaitTimeForRiskReport'/] 
-->
[@ww.label labelKey='blackduckhub.task.scan.scanmemory.label' name='hubScanMemory'/]
[@ww.textarea labelKey='blackduckhub.task.scan.scantargets.label' name='hubTargets' readonly='true'/]