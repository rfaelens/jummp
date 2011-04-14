<p><g:message code="user.resetPassword.ui.explanation"/></p>
<form action="requestPasswordReset" id="password-forgotten-form" class="ui-widget-content">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="password-forgotten-form-username"><g:message code="login.username"/></label></td>
            <td><span><input type="text" name="username" id="password-forgotten-form-username"><jummp:errorField/></span></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'user.resetPassword.ui.requestPassword')}"/>
    </div>
</form>
