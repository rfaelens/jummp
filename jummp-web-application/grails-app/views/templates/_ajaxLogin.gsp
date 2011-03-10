<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<div id="ajaxLoginDialog" style="display: none">
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
        <p>
            <a href="#" onclick="$('#ajaxLoginDialog').dialog('close');showPasswordForgottenView()"><g:message code="login.passwordForgotten"/></a>
        </p>
        <g:if test="${ConfigurationHolder.config.jummpCore.security.anonymousRegistration}">
        <p><g:message code="login.register" args="['$(\'#ajaxLoginDialog\').dialog(\'close\');showRegisterView()']"/></p>
        </g:if>
    </form>
</div>
