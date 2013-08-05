<div id="skip-to">
<ul>
<li><a href="#content">Skip to main content</a></li>
<li><a href="#local-nav">Skip to local navigation</a></li>
<li><a href="#global-nav">Skip to EBI global navigation menu</a></li>
<li><a href="#global-nav-expanded">Skip to expanded EBI global navigation menu (includes all sub-sections)</a></li>
</ul>
</div>

<div id="wrapper" class="container_24">
<header>
<div id="global-masthead" class="masthead grid_24">
<!--This has to be one line and no newline characters-->
<a href="//www.ebi.ac.uk/" title="Go to the EMBL-EBI homepage"><img src="//www.ebi.ac.uk/web_guidelines/images/logos/EMBL-EBI/EMBL_EBI_Logo_white.png" alt="EMBL European Bioinformatics Institute"></a>

<nav>
<ul id="global-nav">
  <!-- set active class as appropriate -->
  <li class="first active" id="services"><a href="//www.ebi.ac.uk/services">Services</a></li>
  <li id="research"><a href="//www.ebi.ac.uk/research">Research</a></li>
  <li id="training"><a href="//www.ebi.ac.uk/training">Training</a></li>
  <li id="industry"><a href="//www.ebi.ac.uk/industry">Industry</a></li>
  <li id="about" class="last"><a href="//www.ebi.ac.uk/about">About us</a></li>
</ul>
</nav>

</div>

<div id="local-masthead" class="masthead grid_24 nomenu">

<!-- local-title -->
<!-- NB: for additional title style patterns, see http://frontier.ebi.ac.uk/web/style/patterns -->
<div id="local-title" class="grid_12 alpha logo-title"> 
	<a href="[service-url]" title="Back to Biomodels homepage">
		<r:img uri="/images/biomodels/logo_small.png"/>
		<%-- <img src="[service-logo]" alt="[service-name] logo" width="x" height="y"> --%>
	</a> 
	<span>
		<h1>Biomodels</h1>
	</span> 
</div>

<!-- /local-title -->

<!-- local-search -->
<!-- NB: if you do not have a local-search, delete the following div, and drop the class="grid_12 alpha" class from local-title above -->

<div class="grid_12 omega">
<form id="local-search" name="local-search" action="[search-action]" method="post">
	
  <fieldset>
  
  <div class="left">
    <label>
    <input type="text" name="first" id="local-searchbox">
    </label>
    <!-- Include some example searchterms - keep them short and few! -->
    <span class="examples">Examples: <a href="[search-url-1]">[search-text-1]</a>, <a href="[search-url-1]">[search-text-2]</a>, <a href="[search-url-1]">[search-text-3]</a></span>
  </div>
  
  <div class="right">
    <input type="submit" name="submit" value="Search" class="submit">          
    <!-- If your search is more complex than just a keyword search, you can link to an Advanced Search,
	 with whatever features you want available -->
    <span class="adv"><a href="../search" id="adv-search" title="Advanced">Advanced</a></span>
  </div>                  
  
  </fieldset>
  
</form>
</div> 
<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/navbar"/>
</div>
</header>
