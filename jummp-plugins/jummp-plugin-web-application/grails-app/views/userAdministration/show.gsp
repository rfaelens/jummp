<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="user.administration.ui.heading.user"/></title>
        <meta name="layout" content="main" />
        <r:require module="userAdministration"/>
    </head>
    <body>
        <h1><g:message code="user.administration.ui.heading.user"/></h1>
        <div>
        <form id="edit-user-form" method="POST">
            <table>
                <thead></thead>
                <tbody>
                <tr>
                    <td><label for="edit-user-username"><g:message code="user.administration.ui.username"/>:</label></td>
                    <td><input type="hidden" id="edit-user-username" name="username" value="${user.username}"/>${user.username}</td>
                </tr>
                <tr>
                    <td><label for="edit-user-userrealname"><g:message code="user.administration.ui.realname"/>:</label></td>
                    <td><span><input type="text" id="edit-user-userrealname" name="userRealName" value="${user.userRealName}"/></span></td>
                </tr>
                <tr>
                    <td><label for="edit-user-email"><g:message code="user.administration.ui.email"/>:</label></td>
                    <td><span><input type="text" id="edit-user-email" name="email" value="${user.email}"/></span></td>
                </tr>
                </tbody>
            </table>
            <div class="buttons">
                <input type="reset" value="${g.message(code: 'user.administration.cancel')}"/>
                <input type="submit" value="${g.message(code: 'user.administration.save')}"/>
            </div>
        </form>
        </div>
        <div id="user-role-management">
            <h1><g:message code="user.administration.userRole.ui.heading" args="[user.username]"/></h1>
            <div id="userRoles">
                <h3><g:message code="user.administration.userRole.ui.heading.usersRoles"/></h3>
                <input type="hidden" value="${user.id}"/>
                <input type="hidden" value="removeRole"/>
                <table>
                    <tbody>
                    <g:each var="role" in="${userRoles}">
                        <tr><td>${role.authority}</td><td><input type="hidden" value="${role.id}"/><a href="#" rel="#userRoles-${role.id}"><g:message code="user.administration.userRole.ui.removeRole"/></a></td></tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div id="availableRoles">
                <h3><g:message code="user.administration.userRole.ui.heading.availableRoles"/></h3>
                <input type="hidden" value="${user.id}"/>
                <input type="hidden" value="addRole"/>
                <table>
                    <tbody>
            <%
                for (def role in roles) {
                    if (userRoles.find { it.id == role.id }) {
                        continue
                    }
            %>
                    <tr><td>${role.authority}</td><td><input type="hidden" value="${role.id}"/><a href="#" rel="#availableRoles-${role.id}"><g:message code="user.administration.userRole.ui.addRole"/></a></td></tr>
            <%
                }
            %>
                    </tbody>
                </table>
            </div>
        </div>
        <r:script>
$(function() {
    $.jummp.userAdministration.editUser();
});
        </r:script>
    </body>
</html>