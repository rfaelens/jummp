<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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











<html>
<head>
    <wcm:widget path="widgets/head"/>
    <wcm:ifContentIs type="org.weceem.blog.WcmBlog">  
        <wcm:feedLink type="rss" path="${node.absoluteURI}"/>  
        <wcm:feedLink type="atom" path="${node.absoluteURI}"/>  
    </wcm:ifContentIs>
</head>
<body>
    <wcm:widget path="widgets/content-head"/>

    <div id="container">
        <wcm:widget path="widgets/navigation"/>
        <wcm:widget path="widgets/branding"/>
        <div id="contentContainer">
            <div id="socialMedia">
            <!-- TODO: integrate social media button or use area for other part -->
            </div>
            <div id="content">
                <div id="main">
                  <!-- If the template is on a Blog node, render list of articles -->
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlog">
                    <wcm:eachChild type="org.weceem.blog.WcmBlogEntry" var="child" max="${node.maxEntriesToDisplay}" sort="publishFrom" order="desc">
                      <!-- Render the BlogEntry using the widget -->
                      <div class="blog-entry">
                        <h1><wcm:link node="${child}">${child.title.encodeAsHTML()}</wcm:link></h1>
                        <wcm:widget model="[node:child]" path="widgets/blog-entry-widget"/>
                      </div>
                    </wcm:eachChild>
                  </wcm:ifContentIs>
                  <!-- If the template is on a BlogEntry node, render just this article -->
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlogEntry">
                        <!-- Render the BlogEntry using the widget -->
                    <h1>${node.title}</h1>
                    <div class="blog-entry">
                      <wcm:widget model="[node:node]" path="widgets/blog-entry-widget"/>
                    </div>
                    <wcm:ifUserCanEdit>
                      <g:link controller="wcmEditor" action="edit" id="${node.id}">Edit</g:link>    
                    </wcm:ifUserCanEdit>
                  </wcm:ifContentIs>
                </div>
                <div id="sideBar">
                  <wcm:ifContentIs type="org.weceem.blog.WcmBlog">
                    <div class="element">
                      <h1>Archive</h1>
                      <h2></h2>
                      <p>
                      <ul>
                        <wcm:archiveList path="${node}">
                          <li><a href="${link}">${wcm.monthName(value:month).encodeAsHTML()} ${year}</a></li>
                        </wcm:archiveList>
                      </ul>
                      </p>
                    </div>
                  </wcm:ifContentIs>
                </div>
            </div>
            <wcm:widget path="widgets/links"/>
        </div>
    </div>
    <wcm:widget path="widgets/footer"/>
</body>
</html>
