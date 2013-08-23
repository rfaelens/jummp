<body class="html front not-logged-in no-sidebars page-node page-node- page-node-1 node-type-page" >
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
    <ul class="menu"><li class="first expanded"><a href="${g.createLink(controller: 'search', action: 'list')}" title="browse models">Browse</a><ul class="menu"><li class="first last leaf"><a href="/content/what-do-workpackages-do">WP overview</a></li>
</ul></li>
<li class="expanded"><a href="${g.createLink(controller: 'model', action: 'create')}" title="submit a model">Submit</a><ul class="menu"><li class="first last leaf"><a href="/partners/people" title="People">People</a></li>
</ul></li>
<li class="leaf"><a href="//ddmore.eu" title="about ddmore">About DDMoRe</a></li>
</ul>  </div>

</div><!-- /.block -->
  </div><!-- /.region -->

  </div></div><!-- /.section, /#header -->
  
</div>

