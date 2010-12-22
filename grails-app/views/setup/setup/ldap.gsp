<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Specify LDAP Information</h1>
            <g:form name="ldapForm" action="setup">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="ldapServer">Server:</label></td>
                                <td class="value"><input type="text" name="ldapServer" id="ldapServer"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapManagerDn">Manager DN:</label></td>
                                <td class="value"><input type="text" name="ldapManagerDn" id="ldapManagerDn"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapManagerPassword">Manager Password:</label></td>
                                <td class="value"><input type="text" name="ldapManagerPassword" id="ldapManagerPassword"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchBase">Search Base:</label></td>
                                <td class="value"><input type="text" name="ldapSearchBase" id="ldapSearchBase"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchFilter">Search Filter:</label></td>
                                <td class="value"><input type="text" name="ldapSearchFilter" id="ldapSearchFilter"/></td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchSubtree">Search Subtree:</label></td>
                                <td class="value"><input type="checkbox" name="ldapSearchSubtree" id="ldapSearchSubtree"/></td>
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
