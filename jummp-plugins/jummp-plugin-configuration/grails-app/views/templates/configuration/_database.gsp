<%@ page import="net.biomodels.jummp.plugins.configuration.DatabaseCommand" %>


<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="databaseType">DB Server Type</label></td>
                <td class="value ${hasErrors(bean: database, field: 'type', 'errors')}">
                    <g:select id="type.key" name='type' value='${database?.type?.key}'
    noSelection="${['null':'Select One...']}"
    from='${net.biomodels.jummp.plugins.configuration.DatabaseType?.list()}'
    optionKey="key" optionValue="value"></g:select>
                    
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="databaseServer">Server (e.g. localhost):</label></td>
                <td class="value ${hasErrors(bean: database, field: 'server', 'errors')}">
                    <input type="text" name="server" id="databaseServer" value="${database ? database.server : 'localhost'}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="databasePort">Port (e.g. 3306):</label></td>
                <td class="value ${hasErrors(bean: database, field: 'port', 'errors')}">
                    <input type="text" name="port" id="databasePort" value="${database ? database.port : '3306'}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="databaseName">Database (name of the database):</label></td>
                <td class="value ${hasErrors(bean: database, field: 'database', 'errors')}">
                    <input type="text" name="database" id="DatabaseName" value="${database?.database}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="databaseUsername">Username:</label></td>
                <td class="value ${hasErrors(bean: database, field: 'username', 'errors')}">
                    <input type="text" name="username" id="databaseUsername" value="${database?.username}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="databasePassword">Password:</label></td>
                <td class="value ${hasErrors(bean: mysql, field: 'password', 'errors')}">
                    <input type="password" name="password" id="databasePassword"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
