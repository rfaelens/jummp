<!DOCTYPE html>
<html>
    <head>
        <title><g:layoutTitle default="Grails" /></title>
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery.cluetip.css')}" type="text/css" />
        <g:render template="/templates/i18n"/>
        <g:javascript library="jquery" plugin="jquery"/>
        <g:javascript src="jquery/jquery.cluetip.js"/>
        <jqui:resources />
        <g:layoutHead />
        <g:javascript>
        $.appName = "${grailsApplication.metadata["app.name"]}";
        </g:javascript>
        <g:javascript src="jummp.js"/>
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="${message(code:'spinner.alt',default:'Loading...')}" />
        </div>
        <g:render template="/templates/ajaxLogin"/>
        <g:render template="/templates/userInfo"/>
        <div id="grailsLogo"><a href="http://grails.org"><img src="${resource(dir:'images',file:'grails_logo.png')}" alt="Grails" border="0" /></a></div>
        <g:layoutBody />
    </body>
</html>