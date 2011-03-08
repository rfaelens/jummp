<div id="registerDialog" style="display: none">
    <form action="register" id="registerForm">
        <div id="registerStatus" class="ui-state-error ui-corner-all" style="display: none">
            <span class="ui-icon ui-icon-alert" rel="icon"></span>
            <ul></ul>
        </div>
        <table>
            <tbody>
            <tr>
                <td><label for="register-form-username"><g:message code="user.register.username"/>:</label></td>
                <td><input type="text" id="register-form-username" name="username"/></td>
            </tr>
            <tr>
                <td><label for="register-form-password"><g:message code="user.register.password"/>:</label></td>
                <td><input type="password" id="register-form-password" name="password"/></td>
            </tr>
            <tr>
                <td><label for="register-form-verifyPassword"><g:message code="user.register.verifyPassword"/>:</label></td>
                <td><input type="password" id="register-form-verifyPassword" name="verifyPassword"/></td>
            </tr>
            <tr>
                <td><label for="register-form-email"><g:message code="user.register.email"/>:</label></td>
                <td><input type="text" id="register-form-email" name="email"/></td>
            </tr>
            <tr>
                <td><label for="register-form-name"><g:message code="user.register.realName"/>:</label></td>
                <td><input type="text" id="register-form-name" name="userRealName"/></td>
            </tr>
            </tbody>
        </table>
    </form>
</div>
