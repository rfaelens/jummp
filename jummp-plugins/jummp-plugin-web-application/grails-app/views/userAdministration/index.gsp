<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>User Administration</title>
        <meta name="layout" content="main" />
        <r:require module="userAdministration"/>
        <jqDT:resources/>
    </head>
    <body>
        <table id="userTable">
            <thead>
            <tr>
                <th><g:message code="user.administration.list.id"/></th>
                <th><g:message code="user.administration.list.username"/></th>
                <th><g:message code="user.administration.list.realname"/></th>
                <th><g:message code="user.administration.list.email"/></th>
                <th><g:message code="user.administration.list.enabled"/></th>
                <th><g:message code="user.administration.list.accountExpired"/></th>
                <th><g:message code="user.administration.list.accountLocked"/></th>
                <th><g:message code="user.administration.list.passwordExpired"/></th>
            </tr>
            </thead>
            <tbody></tbody>
            <tfoot>
            <tr>
                <th><g:message code="user.administration.list.id"/></th>
                <th><g:message code="user.administration.list.username"/></th>
                <th><g:message code="user.administration.list.realname"/></th>
                <th><g:message code="user.administration.list.email"/></th>
                <th><g:message code="user.administration.list.enabled"/></th>
                <th><g:message code="user.administration.list.accountExpired"/></th>
                <th><g:message code="user.administration.list.accountLocked"/></th>
                <th><g:message code="user.administration.list.passwordExpired"/></th>
            </tr>
            </tfoot>
        </table>
        <r:script>
$(function() {
    $.jummp.userAdministration.loadUserList();
});
        </r:script>
    </body>
</html>
