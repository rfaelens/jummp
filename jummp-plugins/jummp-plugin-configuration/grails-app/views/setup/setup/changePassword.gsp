<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${changePassword}">
            <div class="errors">
                <g:renderErrors bean="${changePassword}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Specify Change/Reset Password</h1>
            <g:form name="changePasswordForm" action="setup">
                <g:render template="/templates/configuration/changePassword"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
