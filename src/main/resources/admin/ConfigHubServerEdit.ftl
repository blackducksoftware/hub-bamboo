<html>
  <head>
    <title>$i18n.getText("blackduckhub.config.title.label")</title>
    <meta name="decorator" content="atl.admin">
    $webResourceManager.requireResource("com.blackducksoftware.integration.int-hub-bamboo:webresources")
  </head>
  <body>
    [@ww.form action='editConfig' submitLabelKey='blackduckhub.config.save.label'
              titleKey='blackduckhub.config.title.label'
              descriptionKey='blackduckhub.config.description.label'
              showActionErrors='true']
        [@ww.textfield labelKey='blackduckhub.config.hubUrl.label' name='hubUrl' required='true' /]
        [@ww.textfield labelKey='blackduckhub.config.hubUser.label' name='hubUser' required='true' /]
        [@ww.password labelKey='blackduckhub.config.hubPass.label' name='hubPass' required='true' showPassword='false' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyUrl.label' name='hubProxyUrl' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyPort.label' name='hubProxyPort' /]
        [@ww.textfield labelKey='blackduckhub.config.hubNoProxyHost.label' name='hubNoProxyHost' /]
        [@ww.textfield labelKey='blackduckhub.config.hubProxyUser.label' name='hubProxyUser' /]
        [@ww.password labelKey='blackduckhub.config.hubProxyPass.label' name='hubProxyPass' showPassword='false'/]
    [/@ww.form]
  </body>
</html>