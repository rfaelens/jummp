<nav>
<ul class="grid_24" id="local-nav">
  <li 
  	<g:if test="${g.pageProperty(name:'page.browse')?.length()}">
	    	class="active"
	</g:if>><a href="${g.createLink(controller: 'search', action: 'list')}">Browse</a></li>
  <li
  	<g:if test="${g.pageProperty(name:'page.submit')?.length()}">
	    	class="active"
	</g:if>><a href="${g.createLink(controller: 'model', action: 'create')}">Submit</a></li>
  <li>
  <li
  	<g:if test="${g.pageProperty(name:'page.feedback')?.length()}">
	    	class="active"
	</g:if>><a href="${g.createLink(controller: 'jummp', action: 'feedback')}">Feedback</a></li>
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
