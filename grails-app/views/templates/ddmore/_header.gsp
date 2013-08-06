<div id="wrapper" class="container_24">
<header>
<div id="global-ddmore-masthead" class="masthead grid_24">
<!--This has to be one line and no newline characters-->
<a href="//ddmore.eu/" title="Go to the DDMoRe homepage"><r:img uri="/images/ddmore/logo-small.png"/></a>
<nav>
<ul id="global-nav">
  <!-- set active class as appropriate -->
  <li class="first active" id="project"><a href="http://ddmore.eu/content/project">Project</a></li>
  <li id="partners"><a href="http://ddmore.eu/partners">Partners</a></li>
  <li id="about" class="last"><a href="//ddmore.eu">About us</a></li>
</ul>
</nav>

</div>

<div id="local-masthead" class="masthead grid_24 nomenu">
	<!-- local-title -->
	<!-- NB: for additional title style patterns, see http://frontier.ebi.ac.uk/web/style/patterns -->
	<div id="local-title" class="logo-title"> 
		<a href="${createLink(uri: '/', absolute: true)}" title="Back to DDMoRe Repository homepage">
			<span>
				<h1>Model Repository</h1>
			</span> 
		</a> 
	</div>
	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/navbar"/>
</div>
</header>
