<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${firstRun}">
            <div class="errors">
                <g:renderErrors bean="${firstRun}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Create Admin</h1>
            <p>At the next startup of the web application an admin user can be created. If the database already contains an admin user, there is no need for it.</p>
            <g:form name="firstRun" action="setup">
                <g:render template="/templates/configuration/firstRun"/>
                <div class="buttons">
                    <g:submitButton name="next" value="Finish"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
