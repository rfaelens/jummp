<div id='overlayTable'>
    <div id='overlayTableRow'>
	<div id="loginLogout">
	    <sec:ifLoggedIn>
		<jummp:button class="close logout"><g:message code="jummp.main.logout"/></jummp:button>
	    </sec:ifLoggedIn>
	    <sec:ifNotLoggedIn>
		<jummp:button class="login"><g:message code="jummp.main.login"/></jummp:button>
	    </sec:ifNotLoggedIn>
	</div>
    </div>
</div>
<div id="loginLogout">
    <sec:ifLoggedIn>
	<jummp:button class="close logout"><g:message code="jummp.main.logout"/></jummp:button>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
	<jummp:button class="login"><g:message code="jummp.main.login"/></jummp:button>
    </sec:ifNotLoggedIn>
</div>

