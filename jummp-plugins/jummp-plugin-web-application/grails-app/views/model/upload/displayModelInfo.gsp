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
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Model Information</title>
    </head>
    <body>
        <h2>Model Information</h2>
        <p>Please ensure the following fields are correctly filled in.</p>

        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="name">Name:</label>
                            </td>
                            <td class="value">
                                <g:textField name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label for="description">Description:</label>
                            </td>
                            <td class="value">
                                <g:textArea id="description" name="description" readonly="readonly" maxlength="5000" value='${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}'/>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="Abort" />
                    <g:submitButton name="Back" value="Back" />
                    <g:submitButton name="Continue" value="Continue" />
                </div>
            </div>
        </g:form>
    </body>
    <g:render template="/templates/decorateSubmission" />
    <g:render template="/templates/subFlowContextHelp" />

