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
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="database">Database:</label></td>
                                <td class="value"><input type="radio" name="authenticationBackend" id="database" value="database" checked="checked"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldap">LDAP:</label></td>
                                <td class="value"><input type="radio" name="authenticationBackend" id="ldap" value="ldap"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
