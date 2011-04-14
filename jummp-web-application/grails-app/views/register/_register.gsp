<table>
    <tbody>
    <tr>
        <td><label for="register-form-username"><g:message code="user.register.username"/>:</label></td>
        <td><span><input type="text" id="register-form-username" name="username"/><jummp:errorField/></span></td>
    </tr>
    <g:if test="${password}">
    <tr>
        <td><label for="register-form-password"><g:message code="user.register.password"/>:</label></td>
        <td><span><input type="password" id="register-form-password" name="password"/><jummp:errorField/></span></td>
    </tr>
    <tr>
        <td><label for="register-form-verifyPassword"><g:message code="user.register.verifyPassword"/>:</label></td>
        <td><span><input type="password" id="register-form-verifyPassword" name="verifyPassword"/><jummp:errorField/></span></td>
    </tr>
    </g:if>
    <tr>
        <td><label for="register-form-email"><g:message code="user.register.email"/>:</label></td>
        <td><span><input type="text" id="register-form-email" name="email"/><jummp:errorField/></span></td>
    </tr>
    <tr>
        <td><label for="register-form-name"><g:message code="user.register.realName"/>:</label></td>
        <td><span><input type="text" id="register-form-name" name="userRealName"/><jummp:errorField/></span></td>
    </tr>
    </tbody>
</table>
