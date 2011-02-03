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
function authAjax() {
    $.post("${g.createLink(controller: 'j_spring_security_check')}", $("#ajaxLoginForm").serialize(), function(data) {
        if (data.success) {
            $("#ajaxLoginDialog").dialog('close');
            switchUserInformation(true, data.username);
        } else if (data.error) {
            $("#ajaxLoginStatus").html(data.error);
            $("#ajaxLoginStatus").show();
        }
    });
}
$(document).ready(function() {
    $("#ajaxLoginDialog").dialog({
        autoOpen: false,
        width: 400, // need a slightly larger dialog
        title: "${g.message(code: 'login.authenticate')}",
        buttons: {"${g.message(code: 'login.authenticate')}": authAjax, "${g.message(code: 'login.cancel')}": function() { $(this).dialog("close")}}
    });
});
function showLoginDialog() {
    $("#ajax_j_username").val("");
    $("#ajax_j_password").val("");
    $("#ajaxLoginStatus").hide();
    $('#ajaxLoginDialog').dialog('open');
}
</g:javascript>