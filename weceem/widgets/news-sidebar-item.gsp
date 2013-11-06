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











<div class="element">
  <g:set var="newsBlog" value="${wcm.findNode(path:'news')}"/>
  <h1>News</h1>
  <wcm:eachChild path="${newsBlog}" type="org.weceem.blog.WcmBlogEntry" max="1" sort="publishFrom" order="desc">
    <div class="rss">
        <a href="wcm-tools/feed/rss/jummp/news">
            <img src="${resource(dir: 'images', file: 'feed.png')}"/>
        </a>
    </div>
    <h2><a href="${wcm.createLink(path: it)}">${it.title.encodeAsHTML()}</a></h2>
${it.summary}
  </wcm:eachChild>
  <g:if test="${wcm.countChildren(node: newsBlog) == '0'}">
    <h2>No News published yet</h2>
  </g:if>
  <p>
    <a href="${wcm.createLink(path: 'news')}">more</a>
  </p>
</div>
