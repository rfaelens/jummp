<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>First Run Wizard</title>
    </head>
    <body>
        <div id="remote" class="body yui-skin-sam">
            <h1>Create Admin Account</h1>
            <g:form name="adminForm" action="firstRun">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class='prop'>
                                <td  class='name'><label>Login Name:</label></td>
                                <td  class='value'><input type="text" name='username' /></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Full Name:</label></td>
                                <td  class='value'><input type="text" name='userRealName'/></td>
                            </tr>

                        %{--TODO hide in LDAP setup--}%
                            <tr class='prop'>
                                <td  class='name'><label>Password:</label></td>
                                <td  class='value'><input type="password" name='password'/></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Confirm Password:</label></td>
                                <td  class='value'><input type="password" name='rePassword'/></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Email:</label></td>
                                <td  class='value'><input type="text" name='email'/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="Create"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
