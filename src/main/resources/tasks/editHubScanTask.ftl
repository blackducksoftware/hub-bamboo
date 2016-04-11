[@ww.textfield labelKey='blackduckhub.task.scan.project.label' name='project'/]
[@ww.textfield labelKey='blackduckhub.task.scan.version.label' name='version'/]
[@ww.combobox labelKey='blackduckhub.task.scan.phase.label' name='phase' 
              list="{'In Planning','In Development','Released','Deprecated','Archived'}" emptyOption='false'/]
[@ww.combobox labelKey='blackduckhub.task.scan.distribution.label' name='distribution'
              list="{'External','SaaS','Internal','Open Source'}" emptyOption='false'/]
<!--
[@ww.checkbox labelKey='blackduckhub.task.scan.genriskreport.label' name='generateRiskReport'/]
[@ww.textfield labelKey='blackduckhub.task.scan.maxriskreportwaittime.label' name='maxWaitTimeForRiskReport'/] 
-->
[@ww.textfield labelKey='blackduckhub.task.scan.scanmemory.label' name='scanMemory' required='true'/]
[@ww.textarea labelKey='blackduckhub.task.scan.scantargets.label' name='targets'/]