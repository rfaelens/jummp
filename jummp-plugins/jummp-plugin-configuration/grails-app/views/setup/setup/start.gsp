<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${database}">
            <div class="errors">
                <g:renderErrors bean="${database}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Specify Database Information</h1>
            <g:form name="databaseForm" action="setup">
                <g:render template="/templates/configuration/database"/>
                <div class="buttons">
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
