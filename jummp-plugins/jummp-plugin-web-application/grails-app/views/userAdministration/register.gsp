<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="user.administration.ui.heading.register"/></title>
        <meta name="layout" content="main" />
        <r:require module="userAdministration"/>
    </head>
    <body>
        <h1><g:message code="user.administration.ui.heading.register"/></h1>
        <div>
            <form id="registerForm">
                <table>
                    <tbody>
                    <tr>
                        <td><label for="register-form-username"><g:message code="user.administration.ui.username"/>:</label></td>
                        <td><span><input type="text" id="register-form-username" name="username"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-name"><g:message code="user.administration.ui.realname"/>:</label></td>
                        <td><span><input type="text" id="register-form-name" name="userRealName"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-email"><g:message code="user.administration.ui.email"/>:</label></td>
                        <td><span><input type="text" id="register-form-email" name="email"/></span></td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <input type="reset" value="${g.message(code: 'user.administration.cancel')}"/>
                    <input type="submit" value="${g.message(code: 'user.administration.register')}"/>
                </div>
            </form>
        </div>
        <r:script>
$(function() {
    $.jummp.userAdministration.register();
});
        </r:script>
    </body>
</html>
