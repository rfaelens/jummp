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
        <h2><g:message code="user.administration.updatePassword.heading"/></h2>
        <div>
			<g:form action="updatePassword">
				<table>
					<thead></thead>
					<tbody>
					<tr>
						<td><label><g:message code="user.administration.updatePassword.oldPassword"/>:</label></td>
						<td><g:passwordField name="oldPassword"/></td>
					</tr>
					<tr>
						<td><label><g:message code="user.administration.updatePassword.newPassword"/>:</label></td>
						<td><g:passwordField name="newPassword"/></td>
					</tr>
					<tr>
						<td><label><g:message code="user.administration.updatePassword.newPasswordRpt"/>:</label></td>
						<td><g:passwordField name="newPasswordRpt"/></td>
					</tr>
					</tbody>
				</table>
				<div class="buttons">
						<input type="submit" value="${g.message(code: 'user.administration.updatePassword.submit')}"/>
				</div>
			</g:form>
        </div>
   </body>
</html>