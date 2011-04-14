<g:render template="userData"/>
<g:if test="${changePassword}">
<h2><g:message code="user.change.ui.heading.password"/></h2>
<form id="change-password-form" action="changePassword" method="POST" class="ui-widget-content">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="change-password-old"><g:message code="user.change.ui.oldPassword"/>:</label></td>
            <td><span><input type="password" id="change-password-old" name="oldPassword"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="change-password-new"><g:message code="user.change.ui.newPassword"/>:</label></td>
            <td><span><input type="password" id="change-password-new" name="newPassword"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="change-password-verify"><g:message code="user.change.ui.verifyPassword"/>:</label></td>
            <td><span><input type="password" id="change-password-verify" name="verifyPassword"/><jummp:errorField/></span></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'user.change.ui.changePassword')}"/>
    </div>
</form>
</g:if>
