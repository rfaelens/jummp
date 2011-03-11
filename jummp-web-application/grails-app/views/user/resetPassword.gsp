<h2><g:message code="user.resetPassword.ui.header"/></h2>
<form action="performResetPassword" id="reset-password-form" class="ui-widget-content">
    <input type="hidden" name="code" value="${code}"/>
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="reset-password-form-username"><g:message code="login.username"/></label></td>
            <td><span><input type="text" name="username" id="reset-password-form-username"><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="reset-password-form-password"><g:message code="user.change.ui.newPassword"/></label></td>
            <td><span><input type="password" name="password" id="reset-password-form-password"><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="reset-password-form-verifyPassword"><g:message code="user.change.ui.verifyPassword"/></label></td>
            <td><span><input type="password" name="verifyPassword" id="reset-password-form-verifyPassword"><jummp:errorField/></span></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'user.resetPassword.ui.reset')}"/>
    </div>
</form>
