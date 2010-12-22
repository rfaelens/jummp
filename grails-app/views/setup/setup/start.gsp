<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Specify MySQL Information</h1>
            <g:form name="databaseForm" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="mysqlServer">Server (e.g. localhost):</label></td>
                                <td class="value"><input type="text" name="mysqlServer" id="mysqlServer" value="localhost"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlPort">Port (e.g. 3306):</label></td>
                                <td class="value"><input type="text" name="mysqlPort" id="mysqlPort" value="3306"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlDatabase">Database (name of the database):</label></td>
                                <td class="value"><input type="text" name="mysqlDatabase" id="mysqlDatabase"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlUsername">Username:</label></td>
                                <td class="value"><input type="text" name="mysqlUsername" id="mysqlUsername"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlPassword">Password:</label></td>
                                <td class="value"><input type="password" name="mysqlPassword" id="mysqlPassword"/></td>
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
