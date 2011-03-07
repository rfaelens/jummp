<h2><g:message code="user.change.ui.heading.user"/></h2>
<form action="editUser" id="edit-user-form" method="POST">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="edit-user-username"><g:message code="user.change.ui.username"/>:</label></td>
            <td><input type="hidden" id="edit-user-username" name="username" value="${user.username}"/>${user.username}</td>
        </tr>
        <tr>
            <td><label for="edit-user-userrealname"><g:message code="user.change.ui.realname"/>:</label></td>
            <td><input type="text" id="edit-user-userrealname" name="userRealName" value="${user.userRealName}"/></td>
        </tr>
        <tr>
            <td><label for="edit-user-email"><g:message code="user.change.ui.email"/>:</label></td>
            <td><input type="text" id="edit-user-email" name="email" value="${user.email}"/></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.save')}" onclick="editUser()"/>
    </div>
</form>
<h2><g:message code="user.change.ui.heading.password"/></h2>
<form id="change-password-form" action="changePassword" method="POST">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="change-password-old"><g:message code="user.change.ui.oldPassword"/>:</label></td>
            <td><input type="password" id="change-password-old" name="oldPassword"/></td>
        </tr>
        <tr>
            <td><label for="change-password-new"><g:message code="user.change.ui.newPassword"/>:</label></td>
            <td><input type="password" id="change-password-new" name="newPassword"/></td>
        </tr>
        <tr>
            <td><label for="change-password-verify"><g:message code="user.change.ui.verifyPassword"/>:</label></td>
            <td><input type="password" id="change-password-verify" name="verifyPassword"/></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'user.change.ui.changePassword')}" onclick="changePassword()"/>
    </div>
</form>
