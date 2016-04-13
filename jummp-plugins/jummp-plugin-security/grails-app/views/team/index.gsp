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
    </head>
    <body>
        <g:if test="${flash.message}">
            <div>${flash.message}</div>
        </g:if>
        <p>If you find yourself repeatedly sharing your models with the same people,
        you should consider grouping them into a team. </p>
        <g:if test="${teams}">
            <table>
                <thead>
                    <tr>
                        <th class="spaced">Name</th>
                        <th class="spaced">Description</th>
                        <th class="spaced">Creator</th>
                        <th class="spaced">&nbsp;</th>
                    </tr>
                </thead>
                <tbody>
                    <g:each in="${teams}" var="t" status="i">
                        <tr class="${ (i % 2) == 0 ? 'even' : 'odd'}">
                            <td class="spaced"><g:link action="show" id="${t.id}">${t.name}</g:link></td>
                            <td class="spaced">${t.description}</td>
                            <td class="spaced">${t.owner.person.userRealName}</td>
                            %{--<td class="spaced"><g:link action="delete" id="${t.id}">Delete</g:link></td>--}%
                            <td class="spaced">&nbsp;</td>
                        </tr>
                    </g:each>
                </tbody>
            </table>
        </g:if>
        <span class='spaced'>
            <g:link controller="team" action="create">Create a team</g:link>
        </span>
    </body>
</html>
<content tag="teams">
    selected
</content>
<content tag="title">View my teams</content>
<content tag="contexthelp">
	teams
</content>
