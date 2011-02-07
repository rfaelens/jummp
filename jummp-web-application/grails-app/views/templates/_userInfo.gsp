<div id="userInformation">
    <div id="userInformationLogedIn" style="display: none">
        <span></span> | <span><a href="#" onclick="logout()"><g:message code="logout.title"/></a></span>
    </div>
    <div id="userInformationLogedOut" style="display: none">
        <span><a href="#" onclick="showLoginDialog()"><g:message code="login.authenticate"/></a></span>
    </div>
</div>

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
