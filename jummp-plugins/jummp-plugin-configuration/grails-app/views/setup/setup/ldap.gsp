<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${ldap}">
            <div class="errors">
                <g:renderErrors bean="${ldap}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Specify LDAP Information</h1>
            <g:form name="ldapForm" action="setup">
                <g:render template="/templates/configuration/ldap"/>
                <div class="buttons">
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
