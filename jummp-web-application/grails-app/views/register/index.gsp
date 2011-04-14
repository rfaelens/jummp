<form action="register" id="registerForm" class="ui-widget-content">
    <h2><g:message code="user.register.ui.title"/></h2>
    <g:render template="register"/>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.register')}"/>
    </div>
</form>
