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











<div class="element external">
  <h1>Latest SBML News</h1>
  <div class="rss">
    <a href="http://sbml.org/index.php?title=News&action=feed&feed=rss">
        <img src="${resource(dir: 'images', file: 'feed.png')}"/>
    </a>
  </div>
  <wcm:dataFeed type="atom" url="http://sbml.org/index.php?title=News&action=feed" max="1" custom="true">
    <h2><a href="${item.link.@href}" target="_blank">${item.title.encodeAsHTML()}</a></h2>
<%
def regex = /<\/?(.|\n)*?>/
%>
${item.summary.text().replaceAll(regex, '')}
  </wcm:dataFeed>
  <p><a href="http://sbml.org/News/SBML" target="_blank">more</a></p>
</div>
