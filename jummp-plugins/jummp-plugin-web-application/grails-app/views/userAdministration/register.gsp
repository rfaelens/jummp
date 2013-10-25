<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="user.administration.ui.heading.register"/></title>
        <meta name="layout" content="main" />
        <g:javascript contextPath="" src="useradministration.js"/>
        
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
                    <tr>
                        <td><label for="register-form-institution"><g:message code="user.administration.ui.institution"/>:</label></td>
                        <td><span><input type="text" id="register-form-institution" name="institution"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-orcid"><g:message code="user.administration.ui.orcid"/>:</label></td>
                        <td><span><input type="text" id="register-form-orcid" name="orcid"/></span></td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <input type="reset" value="${g.message(code: 'user.administration.cancel')}"/>
                    <input type="submit" value="${g.message(code: 'user.administration.register')}"/>
                </div>
            </form>
        </div>
        <g:javascript>
$(function() {
    $.jummp.userAdministration.register();
});
        </g:javascript>
    </body>
</html>
