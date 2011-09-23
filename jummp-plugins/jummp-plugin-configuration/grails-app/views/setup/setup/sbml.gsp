<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${sbml}">
            <div class="errors">
                <g:renderErrors bean="${sbml}"/>
            </div>
        </g:hasErrors>
        <div id="sbml" class="body">
            <h1>SBML Settings</h1>
            <g:form name="firstRun" action="setup">
                <g:render template="/templates/configuration/sbml"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
