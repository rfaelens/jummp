<h2><g:message code="user.register.confirm.ui.title"/></h2>
<p>
    <g:if test="${password}">
    <g:message code="user.register.confirm.ui.explanation"/>
    </g:if>
    <g:else>
    <g:message code="user.register.confirm.ui.explanation.noPassword"/>
    </g:else>
</p>
<form action="confirmRegistration" method="POST" id="confirm-registration-form" class="ui-widget-content">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="confirm-registration-form-username"><g:message code="user.register.username"/>:</label></td>
            <td>
                <span><input type="hidden" name="code" value="${code}" />
                <input type="text" name="username" id="confirm-registration-form-username"/><jummp:errorField /></span>
            </td>
        </tr>
        <g:if test="${password}">
        <tr>
            <td><label for="confirm-registration-form-password"><g:message code="user.register.password"/>:</label></td>
            <td><span><input type="password" name="password" id="confirm-registration-form-password"/><jummp:errorField /></span></td>
        </tr>
        <tr>
            <td><label for="confirm-registration-form-verifyPassword"><g:message code="user.register.verifyPassword"/>:</label></td>
            <td><span><input type="password" name="verifyPassword" id="confirm-registration-form-verifyPassword"/><jummp:errorField /></span></td>
        </tr>
        </g:if>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.validate')}"/>
    </div>
</form>
