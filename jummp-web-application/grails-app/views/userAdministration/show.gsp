<%@ page contentType="text/html;charset=UTF-8" %>
<h2><g:message code="user.change.ui.heading.user"/></h2>
<form action="editUser" id="edit-user-form" method="POST" class="ui-widget-content">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="edit-user-username"><g:message code="user.change.ui.username"/>:</label></td>
            <td><input type="hidden" id="edit-user-username" name="username" value="${user.username}"/>${user.username}</td>
        </tr>
        <tr>
            <td><label for="edit-user-userrealname"><g:message code="user.change.ui.realname"/>:</label></td>
            <td><span><input type="text" id="edit-user-userrealname" name="userRealName" value="${user.userRealName}"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="edit-user-email"><g:message code="user.change.ui.email"/>:</label></td>
            <td><span><input type="text" id="edit-user-email" name="email" value="${user.email}"/><jummp:errorField/></span></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.save')}"/>
    </div>
</form>
<div id="user-role-management">
    <h2><g:message code="user.administration.userRole.ui.heading" args="[user.username]"/></h2>
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
