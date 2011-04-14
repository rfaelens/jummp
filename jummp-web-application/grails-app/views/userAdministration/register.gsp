<form action="register" id="registerForm" class="ui-widget-content">
    <g:render template="/register/register"/>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.save')}"/>
    </div>
</form>
