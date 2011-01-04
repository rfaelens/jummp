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
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class="prop">
                                <td class="name"><label for="ldapServer">Server:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapServer', 'errors')}">
                                    <input type="text" name="ldapServer" id="ldapServer" value="${ldap?.ldapServer}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapManagerDn">Manager DN:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapManagerDn', 'errors')}">
                                    <input type="text" name="ldapManagerDn" id="ldapManagerDn" value="${ldap?.ldapManagerDn}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapManagerPassword">Manager Password:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapManagerPassword', 'errors')}">
                                    <input type="text" name="ldapManagerPassword" id="ldapManagerPassword" value="${ldap?.ldapManagerPassword}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchBase">Search Base:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapSearchBase', 'errors')}">
                                    <input type="text" name="ldapSearchBase" id="ldapSearchBase" value="${ldap?.ldapSearchBase}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchFilter">Search Filter:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapSearchFilter', 'errors')}">
                                    <input type="text" name="ldapSearchFilter" id="ldapSearchFilter" value="${ldap?.ldapSearchFilter}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name"><label for="ldapSearchSubtree">Search Subtree:</label></td>
                                <td class="value ${hasErrors(bean: ldap, field: 'ldapSearchSubtree', 'errors')}">
                                    <input type="checkbox" name="ldapSearchSubtree" id="ldapSearchSubtree" ${ldap?.ldapSearchSubtree ? 'checked="checked"' : ''}/>
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
