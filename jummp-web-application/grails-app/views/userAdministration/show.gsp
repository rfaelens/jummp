<%@ page contentType="text/html;charset=UTF-8" %>
<h2><g:message code="user.change.ui.heading.user"/></h2>
<g:render template="/user/userData"/>
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
