<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



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
      	<div id="block-system-user-menu" class="block block-system user-menu block-menu first odd">      
      		<div class="content">
      			<ul class="menu">
      			<sec:ifLoggedIn>
      				<li class="first last leaf">
      					<a href="${grailsApplication.config.grails.serverURL}/logout">
      						<g:message code="jummp.main.logout"/>
      					</a>
      				</li>
      			</sec:ifLoggedIn>
      			<sec:ifNotLoggedIn>
      				<li class="first leaf">
      					<a href="${grailsApplication.config.grails.serverURL}/registration">
      						<g:message code="jummp.main.register"/>
      					</a>
      				</li>
      				<li class="last leaf">
      					<a href="${grailsApplication.config.grails.serverURL}/login">
      						<g:message code="jummp.main.login"/>
      					</a>
      				</li>
      			</sec:ifNotLoggedIn>
      			</ul>
      		</div><!-- /.block -->
      	</div>
      <div id="block-search-form" class="block block-search search even">
      	 <div class="content">
      	 	<g:form controller="search" action="searchRedir">
      	 	<%-- <form action="/" method="post" id="search-block-form" accept-charset="UTF-8"> --%><div><div class="container-inline">
      	 		<h2 class="element-invisible">Search form</h2>
      	 		<div class="form-item form-type-textfield form-item-search-block-form">
      	 			<label class="element-invisible" for="edit-search-block-form--2">Search </label>
      	 			<input title="Enter the terms you wish to search for." type="text" id="edit-search-block-form--2" name="search_block_form" value="" size="15" maxlength="128" class="form-text" />
      	 		</div>
      	 		<div class="form-actions form-wrapper" id="edit-actions">
      	 			<input type="submit" id="edit-submit" name="op" value="Search" class="form-submit" />
      	 		</div>
			</div>
</div></g:form>  </div>

</div>
      	 		
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
    	<li class="expanded">
    		<a href="${g.createLink(controller: 'jummp', action: 'feedback')}" 
    		<g:if test="${g.pageProperty(name:'page.feedback')?.length()}">
	    		class="active-trail active"
	    	</g:if>
    		title="give feedback">Feedback</a>
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

