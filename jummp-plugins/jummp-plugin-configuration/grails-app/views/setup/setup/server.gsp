<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${server}">
            <div class="errors">
                <g:renderErrors bean="${server}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Server Settings</h1>
            <g:form name="firstRun" action="setup">
                <g:render template="/templates/configuration/server"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
