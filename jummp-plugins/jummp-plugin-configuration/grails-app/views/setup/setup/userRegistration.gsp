<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${userRegistration}">
            <div class="errors">
                <g:renderErrors bean="${userRegistration}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Specify User Registration</h1>
            <g:form name="userRegistrationForm" action="setup">
                <g:render template="/templates/configuration/userRegistration"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
