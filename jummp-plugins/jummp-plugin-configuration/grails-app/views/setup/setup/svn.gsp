<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${svn}">
            <div class="errors">
                <g:renderErrors bean="${svn}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Version Control System - Subversion</h1>
            <p>The Subversion backend only provides checking out from a local repository.</p>
            <g:form name="svn" action="setup">
                <g:render template="/templates/configuration/svn"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
