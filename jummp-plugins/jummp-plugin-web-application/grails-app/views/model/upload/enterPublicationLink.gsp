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
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>
<%@ page import="net.biomodels.jummp.core.model.ModelTransportCommand" %>
<%@ page import=" net.biomodels.jummp.model.PublicationLinkProvider" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.publicationLink.header"/></title>
    </head>
    <body>
        <h2><g:message code="submission.publicationLink.header"/></h2>
        <g:form>
            <g:message code="submission.publink.publication"/>
            <%
                model = workingMemory.get('ModelTC') as ModelTransportCommand
                revision = workingMemory.get("RevisionTC") as RevisionTransportCommand
                publication = revision?.model?.publication
            %>
            <g:if test="${publication}">
                Currently, the model is associated with: 
                <g:render  model="[model:model]" template="/templates/showPublication" />
            </g:if>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                         <tr class="prop">
                            <td class="value" style="vertical-align:top;">
                                <g:if test="${publication}">
                                            <g:select name="PubLinkProvider"
                                            from="${PublicationLinkProvider.LinkType.
                                                    values().collect { it.toString() }}"
                                                    value="${publication.linkProvider.linkType.toString()}"
                                                    noSelection="['':'-Please select publication link type-']"/>
                                            <g:textField name="PublicationLink"
                                                         value="${publication.link}"/>
                                </g:if>
                                <g:else>
                                            <g:select name="PubLinkProvider"
                                            from="${PublicationLinkProvider.LinkType.
                                                    values().collect { it.toString() }}"
                                            noSelection="['':'-Please select publication link type-']"/>
                                            <g:textField name="PublicationLink"/>
                                </g:else>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                   	<g:submitButton name="Continue" value="${g.message(code: 'submission.publink.continueButton')}"/>
                </div>
            </div>
        </g:form>
    </body>
   <g:render template="/templates/decorateSubmission" />
   <g:render template="/templates/subFlowContextHelp" />

