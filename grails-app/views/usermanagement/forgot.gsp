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











<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Forgot Password</title>
        </style>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
    </head>
     <body>
        <p><g:message code="user.forgot.ui.explanation"/></p>
        <div>
            <g:form name="passwordForm" action="requestPassword">
                <table>
                    <tbody>
                    <tr>
                        <td class='tableLabels'><label><g:message code="user.forgot.ui.username"/>:</label></td>
                        <td><span><g:textField name="username"/></span></td>
                    </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <input type="submit" value="Submit"/>
                </div>
            </g:form>
        </div>
        </body>
</html>
<content tag="title">
	<g:message code="user.forgot.ui.heading"/>
</content>
