<div id="userInformation">
    <div id="userInformationLogedIn" style="display: none">
        <span></span> | <span><a href="#" onclick="logout()">Logout</a></span>
    </div>
    <div id="userInformationLogedOut" style="display: none">
        <span><a href="#" onclick="showLoginDialog()">Login</a></span>
    </div>
</div>

<g:javascript>
function logout() {
    $.ajax({ url: "${g.createLink(controller: 'logout')}",
        success: function(data, textStatus, jqXHR) {
            $(document).trigger("logout");
        }
    });
}

function switchUserInformation(logedIn, userName) {
    if (logedIn) {
        if (userName) {
            $("#userInformationLogedIn span").first().text(userName);
        }
        $("#userInformationLogedIn").show();
        $("#userInformationLogedOut").hide();
    } else {
        $("#userInformationLogedIn").hide();
        $("#userInformationLogedOut").show();
    }
}
</g:javascript>
<sec:ifLoggedIn>
    <g:javascript>
    $(document).ready(function() {
        switchUserInformation(true, "${sec.username()}");
    });
    </g:javascript>
</sec:ifLoggedIn>
<sec:ifNotLoggedIn>
    <g:javascript>
    $(document).ready(function() {
        switchUserInformation(false);
    });
    </g:javascript>
</sec:ifNotLoggedIn>
