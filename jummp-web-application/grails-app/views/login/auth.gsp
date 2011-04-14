<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
<head>
<meta name='layout' content='main' />
<title>Welcome to JUMMP</title>
</head>

<body>
    <div id="body" class="ui-widget">
        <div id="status-code" style="display: none">403</div>
        <div id="authenticated" style="display: none">false</div>
        <form action="${postUrl}" method="POST" id="loginForm" class="ui-widget-content">
        <table>
            <tbody>
            <tr>
                <td>
                    <label for="j_username"><g:message code="login.username"/></label>
                </td>
                <td>
                    <input type="text" id="j_username" name="j_username"/>
                </td>
            </tr>
            <tr>
                <td>
                    <label for="j_password"><g:message code="login.password"/></label>
                </td>
                <td>
                    <input type="password" id="j_password" name="j_password"/>
                </td>
            </tr>
            </tbody>
        </table>
        <p>
            <a href="#" onclick="loadView('${g.createLink(controller: 'user', action: 'passwordForgotten')}', loadPasswordForgottenCallback)"><g:message code="login.passwordForgotten"/></a>
        </p>
        <g:if test="${ConfigurationHolder.config.jummpCore.security.anonymousRegistration}">
        <p><g:message code="login.register" args="['showRegisterView()']"/></p>
        </g:if>
        <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
            <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
            <input type="submit" value="${g.message(code: 'login.authenticate')}"/>
        </div>
        </form>
        <g:javascript>
        $(document).ready(function() {
            $("#loginForm div input").button();
            <g:if test="${flash.message}">
            showErrorMessage("${flash.message}");
            </g:if>
        });
        </g:javascript>
    </div>
</body>
