<div id="ajaxLoginDialog">
    <form action="" id="ajaxLoginForm">
        <div id="ajaxLoginStatus" class="ui-state-error" style="display: none"></div>
        <table>
            <tbody>
            <tr>
                <td>
                    <label for="ajax_j_username"><g:message code="login.username"/></label>
                </td>
                <td>
                    <input type="text" id="ajax_j_username" name="j_username"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="ajax_j_password"><g:message code="login.password"/></label>
                </td>
                <td>
                    <input type="password" id="ajax_j_password" name="j_password"/>
                </td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
<g:javascript>
$(document).ready(function() {
    $("#ajaxLoginDialog").dialog({
        autoOpen: false,
        width: 400, // need a slightly larger dialog
        title: i18n.login.authenticate,
        buttons: [
            {
                text: i18n.login.authenticate,
                click: authAjax
            },
            {
                text: i18n.login.cancel,
                click: function() { $(this).dialog("close")}
            }
        ]
    });
});
</g:javascript>