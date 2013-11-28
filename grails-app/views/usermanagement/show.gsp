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




<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="user.administration.ui.heading.user"/></title>
        <meta name="layout" content="main" />
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
     </head>
    <body>
        <div>
			<table>
				<thead></thead>
				<tbody>
				<tr>
					<td><label><g:message code="user.administration.ui.username"/>:</label></td>
					<td>${user.username}</td>
				</tr>
				<tr>
					<td><label><g:message code="user.administration.ui.realname"/>:</label></td>
					<td>${user.userRealName}</td>
				</tr>
				<tr>
					<td><label><g:message code="user.administration.ui.email"/>:</label></td>
					<td>${user.email}</td>
				</tr>
				<tr>
					<td><label><g:message code="user.administration.ui.institution"/>:</label></td>
					<td>${user.institution}</td>
				</tr>
				<tr>
					<td><label><g:message code="user.administration.ui.orcid"/>:</label></td>
					<td>${user.orcid}</td>
				</tr>
				</tbody>
			</table>
			<ul id="optionsList">
				<li><a href='<g:createLink action="edit"/>'>Edit User</a></li>
				<li><a href='<g:createLink action="editPassword" />'>Change Password</a></li>
			</ul>
        </div>
   </body>
</html>
<content tag="title">
	${user.userRealName}'s Profile
</content>
