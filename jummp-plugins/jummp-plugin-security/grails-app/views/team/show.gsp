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

<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>View team</title>
    </head>
    <body>
        <g:if test="${team.description}">
        <div>
            <strong>Description </strong><span>${team.description}</span>
        </div>
        </g:if>
        <div>
            <h2>Members</h2>
            	<ul>
            		<g:each in="${users}">
            			<li>${it.userRealName}</it>
            		</g:each>
            	</ul>
        </div>
        <sec:ifLoggedIn>
      		<g:if test="${sec.username() == team.owner.username}">
      			<a href="${createLink(action:'edit', id:team.id)}">Edit Team</a>
      		</g:if>
      	</sec:ifLoggedIn>
    </body>
</html>
<content tag="title">View team '${team.name}'</content>
<content tag="contexthelp">
	teams
</content>