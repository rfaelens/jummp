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
        <title><g:message code="user.administration.ui.heading.register"/></title>
        <meta name="layout" content="main" />
        <g:javascript contextPath="" src="useradministration.js"/>
        
    </head>
    <body>
        <div>
            <form id="registerForm">
                <table>
                    <tbody>
                    <tr>
                        <td><label for="register-form-username"><g:message code="user.administration.ui.username"/>:</label></td>
                        <td><span><input type="text" id="register-form-username" name="username"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-name"><g:message code="user.administration.ui.realname"/>:</label></td>
                        <td><span><input type="text" id="register-form-name" name="userRealName"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-email"><g:message code="user.administration.ui.email"/>:</label></td>
                        <td><span><input type="text" id="register-form-email" name="email"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-institution"><g:message code="user.administration.ui.institution"/>:</label></td>
                        <td><span><input type="text" id="register-form-institution" name="institution"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="register-form-orcid"><g:message code="user.administration.ui.orcid"/>:</label></td>
                        <td><span><input type="text" id="register-form-orcid" name="orcid"/></span></td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <input type="reset" value="${g.message(code: 'user.administration.cancel')}"/>
                    <input type="submit" value="${g.message(code: 'user.administration.register')}"/>
                </div>
            </form>
        </div>
        <g:javascript>
$(function() {
    $.jummp.userAdministration.register();
});
        </g:javascript>
    </body>
</html>
<content tag="title">
	<g:message code="user.administration.ui.heading.register"/>
</content>
