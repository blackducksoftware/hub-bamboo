<html>
  <head>
    <meta name="decorator" content="atl.admin">
    <title>[#assign titleText = 'blackduckhub.config.title.label'/]</title>
  </head>
  <body>
    [@ww.form action='configServerDetails' submitLabelKey='blackduckhub.config.save.label'
              titleKey='blackduckhub.config.title.label'
              descriptionKey='blackduckhub.config.description.label'
              showActionErrors='true']
        [@ww.textfield labelKey='blackduckhub.config.hubUrl.label' name='hubUrl' required='true' /]
        [@ww.textfield labelKey='blackduckhub.config.hubUser.label' name='hubUser' required='true' /]
        [@ww.password labelKey='blackduckhub.config.hubPass.label' name='hubPass' required='true' showPassword='true' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyUrl.label' name='hubProxyUrl' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyPort.label' name='hubProxyPort' /]
        [@ww.textfield labelKey='blackduckhub.config.hubNoProxyHost.label' name='hubNoProxyHost' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyUser.label' name='hubProxyUser' /]
        [@ww.password labelKey='blackduckhub.config.hubProxyPass.label' name='hubProxyPass' showPassword='true'/]
    [/@ww.form] [@ww.form action='testConnection' submitLabelKey='blackduckhub.config.testconnection.label' showActionErrors='true'] [/@ww.form]
  </body>
</html>