<g:if test="${g.pageProperty(name:'page.sidebar')?.length()}">
    <body class="html not-front not-logged-in one-sidebar sidebar-second page-node page-node- page-node-37 node-type-webform section-content">
</g:if>
<g:else>
    <body class="html front not-logged-in no-sidebars page-node page-node- page-node-1 node-type-page" >
</g:else>
  <div id="skip-link">
    <a href="#main-menu" class="element-invisible element-focusable">Jump to Navigation</a>
  </div>
    
<div id="header-full">
  
   <div id="header"><div class="section clearfix">

          <a href="${createLink(uri: '/', absolute: true)}" title="Home" rel="home" id="logo"><img src="http://www.ddmore.eu/sites/ddmore/themes/ddmore/logo.png" alt="Home" /></a>
    
    
    
      <div class="region region-header">
    <div style="float:right;margin-top:5px;">

      
  <div class="content">
     <sec:ifLoggedIn>
    	<a href="/jummp/logout" class="icon icon-functional" data-icon="l">
		<g:message code="jummp.main.logout"/>
	</a>
    </sec:ifLoggedIn>
    <sec:ifNotLoggedIn>
	<a href="/jummp/login" class="icon icon-functional" data-icon="l">
		<g:message code="jummp.main.login"/>
	</a>
    </sec:ifNotLoggedIn>
</div><!-- /.block -->
<div id="block-system-main-menu" class="block block-system main-menu block-menu last odd">

      
  <div class="content">
    <ul class="menu">
    	<li class="first expanded active-trail">
    		<a href="${g.createLink(controller: 'search', action: 'list')}" 
    		<g:if test="${g.pageProperty(name:'page.browse')?.length()}">
	    		class="active-trail active"
	    	</g:if>
    		title="browse models">Browse</a>
    	</li>
    	<li class="expanded">
    		<a href="${g.createLink(controller: 'model', action: 'create')}" 
    		<g:if test="${g.pageProperty(name:'page.submit')?.length()}">
	    		class="active-trail active"
	    	</g:if>
    		title="submit a model">Submit</a>
    	</li>
    	<li class="leaf">
    		<a href="//ddmore.eu" title="about ddmore">About DDMoRe</a>
    	</li>
    </ul>  
</div>
<p>${g.pageProperty(name:'page.selectedtab')}</p>
</div><!-- /.block -->
  </div><!-- /.region -->

  </div></div><!-- /.section, /#header -->
  
</div>

