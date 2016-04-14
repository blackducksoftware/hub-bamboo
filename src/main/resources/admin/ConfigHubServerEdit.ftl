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