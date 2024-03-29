<%--
 Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>











<div id="center-full">

  <div id="page-wrapper">
    <div id="center-background">&nbsp;</div>
    <div id="center-backgroundright">&nbsp;</div>
    <div id="page">

  <div id="main-wrapper"><div id="main" class="clearfix">

    <div id="content" class="column"><div class="section">
            
                  
            <a id="main-content"></a>
                    <h1 class="title" id="page-title">
                    	<g:pageProperty name="page.title" />
                    </h1>
                    <g:render template="/templates/notification/showNotificationDiv"/>
  		            <div class="content">
                     </div>
                                <div class="region region-content">
    <div id="block-system-main" class="block block-system first last odd">

      
  <div class="content">
    
  	<div class="content pagecontent">
  		<g:pageProperty name="page.main-content" />
  		<g:layoutBody/>
  	</div>
  </div>

  </div><!-- /.region -->
          </div>

    </div><!-- /.section, /#content -->

    
 
<%
  	def sidebarContent=g.pageProperty(name:'page.sidebar')
  	if (sidebarContent) {
  		sidebarContent=sidebarContent.trim()
  	}
%>   
    
  </div>
  <g:if test="${sidebarContent}">
  	<div class="region region-sidebar-second column sidebar">
 		${sidebarContent}
 	</div>
 </g:if> 
 
  
  </div><!-- /#main, /#main-wrapper -->
  
  

