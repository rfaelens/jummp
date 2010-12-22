<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Create Admin</h1>
            <p>At the next startup of the web application an admin user can be created. If the database already contains an admin user, there is no need for it.</p>
            <g:form name="firstRun" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="create">Create Admin User:</label></td>
                                <td class="value"><input type="radio" name="firstRun" id="create" value="true" checked="checked"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="reuse">Reuse existing user in Database:</label></td>
                                <td class="value"><input type="radio" name="firstRun" id="reuse" value="false"/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="Finish"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
