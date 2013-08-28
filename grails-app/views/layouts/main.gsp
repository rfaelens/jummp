<!doctype html>
<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/precursor" />
<html>
<head>
    <g:javascript library="jquery" plugin="jquery"/>
    <g:javascript src="jquery/jquery.i18n.properties-min-1.0.9.js"/>
    <g:javascript>
    	$.appName = "${grailsApplication.metadata["app.name"]}";
    </g:javascript>
    <%-- <jqui:resources themeCss="css/jqueryui/smoothness/jquery-ui-1.10.3.custom.min.css"/>
     --%><g:javascript src="jummp.js"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/head" />
    <g:layoutHead/>
</head>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/header"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/mainbody"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/footer"/>
</html>
