<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Register</title>
        <style>
        	.verysecure {
        		visibility:hidden;
        	}
        </style>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
        <script>
        	$(document).ready(function() {
        		setTimeout(function(){
					$(".flash").fadeOut("slow", function () {
							$(".flash").remove();
					}); }, 4000);
        	});
        </script>
    </head>
     <body>
        <h2><g:message code="user.signup.ui.heading.register"/></h2>
        <g:if test="${flashMessage && flashMessage.length()>0}">
	    		<div class='flash'>${flashMessage}</div>
	    </g:if>
        <div>
            <g:form name="registerForm" action="signUp">
                <table>
                    <tbody>
                    <tr>
                        <td><label for="register-form-username"><g:message code="user.signup.ui.username"/>:</label></td>
                        <td><span><g:textField name="username"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-name"><g:message code="user.signup.ui.realname"/>:</label></td>
                        <td><span><g:textField name="realname"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-email"><g:message code="user.signup.ui.email"/>:</label></td>
                        <td><span><g:textField name="email"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-institution"><g:message code="user.signup.ui.institution"/>:</label></td>
                        <td><span><g:textField name="institution"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-orcid"><g:message code="user.signup.ui.orcid"/>:</label></td>
                        <td><span><g:textField name="orcid"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-captcha"><g:message code="user.signup.ui.captcha"/>:</label></td>
                        <td>
                        	<ul style="list-style-type: none;">
                        		<li><img style="margin-top:0;float:none" src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}"/></li>
                        		<li><g:textField name="captcha"/></li>
                        	</ul>
                        </td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <input type="submit" value="${g.message(code: 'user.signup.register')}"/>
                </div>
                <label class="verysecure">You shouldnt see me</label>
                <input class="verysecure" name="securityfeature" value=""/>
                 
            </g:form>
        </div>
        </body>
</html
