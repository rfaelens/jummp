<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Authentication Backend</h1>
            <p>Jummp can either verify usernames against the database or against an LDAP. If you select LDAP you can configure the required settings in the next step</p>
            <g:form name="authenticationBackend" action="setup">
                <g:render template="/templates/configuration/authenticationBackend"/>
                <div class="buttons">
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
