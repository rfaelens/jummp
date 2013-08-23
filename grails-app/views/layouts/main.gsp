<!doctype html>
<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/precursor" />
<html>
<head>
    <title><g:layoutTitle default="${g.message(code: 'jummp.main.title') }"/></title>
    <r:script>
    	$.appName = "${grailsApplication.metadata["app.name"]}";
    </r:script>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/head" />
    <r:layoutResources/>
    <g:layoutHead/>
</head>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/header"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/mainbody"/>
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/footer"/>
    <r:layoutResources/>
</html>
