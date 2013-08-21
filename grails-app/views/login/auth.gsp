<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Login</title>
    </head>
    <body>
        <h2>Login</h2>
    <div id='login'>
        <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
            <table>
                <tr><p>${flash.message}</p></tr>
                <tbody>
                    <tr>
                    	<td class="name">
                                <label for="username">
                                    <g:message code="login.form.label"/>
                                </label>
                        </td>
                        <td>
                        	<input type='text' name='j_username' id='username'/>
                        </td>
                    </tr>
                    <tr>
  	                <td class="name">
                               <label for="password">
                                    <g:message code="login.form.password"/>
                                </label>
                        </td>
                    	<td>
                    		<input type='password' name='j_password' id='password'/>
                    	</td>
                   </tr>
                    <tr>
  	                <td/>
                    	<td>
                    		<button>LOGIN</button>
                    	</td>
                   </tr>
                </tbody>
            </table>
        </form>
    </div>
    <script type='text/javascript'>
    <!-- TODO: move out of HTML page //-->
    	$("#loginForm input").focus(function() {
    		if ($(this).data("reset") === undefined) {
    		$(this).val("");
    		$(this).data("reset", true);
    		}
    	});
    	$("#loginForm input").keyup(function(event) {
    	// magic value 13 is enter
    	if (event.which == 13) {
        	$("#loginForm").submit();
        	}
        });
        $("#login div.loginButton button").click(function() {
        	$("#loginForm").submit();
        });
    </script>
    </body>
</html
