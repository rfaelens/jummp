<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${mysql}">
            <div class="errors">
                <g:renderErrors bean="${mysql}"/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Specify MySQL Information</h1>
            <g:form name="databaseForm" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="mysqlServer">Server (e.g. localhost):</label></td>
                                <td class="value ${hasErrors(bean: mysql, field: 'server', 'errors')}">
                                    <input type="text" name="server" id="mysqlServer" value="${mysql ? mysql.server : 'localhost'}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlPort">Port (e.g. 3306):</label></td>
                                <td class="value ${hasErrors(bean: mysql, field: 'port', 'errors')}">
                                    <input type="text" name="port" id="mysqlPort" value="${mysql ? mysql.port : '3306'}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlDatabase">Database (name of the database):</label></td>
                                <td class="value ${hasErrors(bean: mysql, field: 'database', 'errors')}">
                                    <input type="text" name="database" id="mysqlDatabase" value="${mysql?.database}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlUsername">Username:</label></td>
                                <td class="value ${hasErrors(bean: mysql, field: 'username', 'errors')}">
                                    <input type="text" name="username" id="mysqlUsername" value="${mysql?.username}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="mysqlPassword">Password:</label></td>
                                <td class="value ${hasErrors(bean: mysql, field: 'password', 'errors')}">
                                    <input type="password" name="password" id="mysqlPassword"/>
                                </td>
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
