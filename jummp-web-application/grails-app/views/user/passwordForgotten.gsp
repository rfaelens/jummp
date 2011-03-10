<p><g:message code="user.resetPassword.ui.explanation"/></p>
<form action="requestPasswordReset" id="password-forgotten-form">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="password-forgotten-form-username"><g:message code="login.username"/></label></td>
            <td><input type="text" name="username" id="password-forgotten-form-username"></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'user.resetPassword.ui.requestPassword')}" onclick="requestPassword()"/>
    </div>
</form>
