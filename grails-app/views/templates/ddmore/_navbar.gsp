<nav>
<ul class="grid_24" id="local-nav">
  <li class="first"><a href="${g.createLink(controller: 'search', action: 'list')}">Browse</a></li>
  <li><a href="${g.createLink(controller: 'model', action: 'create')}">Submit</a></li>
  <!-- If you need to include functional (as opposed to purely navigational) links in your local menu,
       add them here, and give them a class of "functional". Remember: you'll need a class of "last" for
       whichever one will show up last... 
       For example: -->
  <li class="functional last">
    <sec:ifLoggedIn>
    	<a href="/jummp/logout" class="icon icon-functional" data-icon="l">
		<g:message code="jummp.main.logout"/>
	</a>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
	<a href="/jummp/login" class="icon icon-functional" data-icon="l">
		<g:message code="jummp.main.login"/>
	</a>
    </sec:ifNotLoggedIn></a></li>
</ul>
</nav>    
