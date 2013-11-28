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
<%@ page import="net.biomodels.jummp.core.model.ModelTransportCommand" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.summary.header"/></title>
    </head>
    <body>
    	<h2><g:message code="submission.summary.header"/></h2>
        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="name">
                                    <g:message code="submission.summary.nameLabel"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="description">
                                    <g:message code="submission.summary.descriptionLabel"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                            	<div class="displayDescription">
                                	${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}
                                </div>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="description">
                                    <g:message code="submission.summary.publication"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                            	<div class="displayDescription">
                            		<%
                            			model=(workingMemory.get("ModelTC") as ModelTransportCommand)
                            		%>
                            		<g:render  model="[model:model]" template="/templates/showPublication" />
                                </div>
                            </td>
                        </tr>
                        <g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
                            <tr class="prop">
                                <td class="name">
                                    <label for="RevisionComments">
                                    	<g:message code="submission.summary.revisionLabel"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:textArea name="RevisionComments" rows="5" cols="70"/>
                                </td>
                          </tr>
                        </g:if>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
                    	<g:submitButton name="Continue" value="${g.message(code: 'submission.summary.update.submitButton')}"/>
                    </g:if>
                    <g:else>
                    	<g:submitButton name="Continue" value="${g.message(code: 'submission.summary.create.submitButton')}"/>
                    </g:else>
                </div>
            </div>
        </g:form>
    </body>
   <g:render template="/templates/decorateSubmission" />
