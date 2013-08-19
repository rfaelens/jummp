<!doctype html>
<html>
<head>
    <title>Login</title>
    <link rel="shortcut icon" href="${g.createLink(uri: '/images/favicon.ico')}"/>
    <r:require module="style"/>
    <r:require module="core"/>
    <r:layoutResources/>
</head>

<body class="login">
    <div id="topBackground"></div>
    <div id="loginBackground"></div>
    <div id="logo"></div>
    <div id='login'>
        <form action='${postUrl}' method='POST' id='loginForm' class='cssform' autocomplete='off'>
            <table>
                <tbody>
                    <tr><td><p>${flash.message}</p></td></tr>
                    <tr><td><input type='text' class='text_' name='j_username' id='username'  value="LOGIN"/></td></tr>
                    <tr><td><input type='password' class='text_' name='j_password' id='password' value="PASSWORD"/></td></tr>
                </tbody>
            </table>
        </form>
        <div class="loginButton">
            <button>LOGIN</button>
            <div class="glow"></div>
        </div>
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
$("#login div.loginButton button").hover(function() {
    $("#login div.glow").show();
}, function() {
    $("#login div.glow").hide();
});
</script>
    <r:layoutResources/>
</body>
</html>
