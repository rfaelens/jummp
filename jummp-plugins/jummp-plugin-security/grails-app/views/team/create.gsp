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
        <title>Create a team</title>
    </head>
    <body>
        <g:form name="newTeamForm" action="save">
            <table>
                <tbody>
                    <tr>
                        <td><label class="required" for="name">Name</label></td>
                        <td><span><g:textField required="true" autofocus="true" maxlength="255" name="name"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="description">Description</label></td>
                        <td><span><g:textField maxlength="255" name="description"/></span></td>
                    </tr>
                </tbody>
            </table>
            <g:submitButton name="create" value="Create"/>
        </g:form>
    </body>
</html>
<content tag="title">Create a team</content>
