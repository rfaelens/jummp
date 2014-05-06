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
<html>
    <head>
        <title><g:message code="user.administration.ui.heading.user"/></title>
        <meta name="layout" content="main" />
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
     </head>
    <body>
        <div>
			<g:form action="editUser">
				<table>
					<thead></thead>
					<tbody>
					<tr>
						<td><label for="edit-user-username"><g:message code="user.administration.ui.username"/>:</label></td>
						<td><input type="hidden" id="edit-user-username" name="username" value="${user.username}"/>${user.username}</td>
					</tr>
					<tr>
						<td><label for="edit-user-userrealname"><g:message code="user.administration.ui.realname"/>:</label></td>
						<td><span><input type="text" id="edit-user-userrealname" name="userRealName" value="${user.person.userRealName}"/></span></td>
					</tr>
					<tr>
						<td><label for="edit-user-email"><g:message code="user.administration.ui.email"/>:</label></td>
						<td><span><input type="text" id="edit-user-email" name="email" value="${user.email}"/></span></td>
					</tr>
					<tr>
						<td><label for="edit-user-institution"><g:message code="user.administration.ui.institution"/>:</label></td>
						<td><span><input type="text" id="edit-user-institution" name="institution" value="${user.person.institution}"/></span></td>
					</tr>
					<tr>
						<td><label for="edit-user-orcid"><g:message code="user.administration.ui.orcid"/>:</label></td>
						<td><span><input type="text" id="edit-user-orcid" name="orcid" value="${user.person.orcid}"/></span></td>
					</tr>
					</tbody>
				</table>
				<div class="buttons">
						<input type="submit" value="${g.message(code: 'user.administration.edit')}"/>
				</div>
			</g:form>
        </div>
   </body>
</html>
<content tag="title">
	<g:message code="user.administration.ui.heading.user"/>
</content>
