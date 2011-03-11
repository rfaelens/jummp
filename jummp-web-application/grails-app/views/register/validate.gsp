<h2><g:message code="user.register.validate.ui.title"/></h2>
<p><g:message code="user.register.validate.ui.explanation"/></p>
<form action="validateRegistration" method="POST" id="validate-registration-form" class="ui-widget-content">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="validate-registration-form-username"><g:message code="user.register.username"/>:</label></td>
            <td>
                <input type="hidden" name="code" value="${code}" />
                <input type="text" name="username" id="validate-registration-form-username"/>
            </td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.validate')}"/>
    </div>
</form>