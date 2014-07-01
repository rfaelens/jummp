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











<div id="model-information-summary">
    <h2><g:message code="model.summary.information"/></h2>
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><strong><g:message code="model.summary.model.id"/></strong></td>
            <td>${revision.model.id}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.submitter"/></strong></td>
            <td>${revision.model.submitter}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.submissionDate"/></strong></td>
            <td>${revision.model.submissionDate}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.id"/></strong></td>
            <td>${revision.revisionNumber}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.uploadDate"/></strong></td>
            <td>${revision.uploadDate}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.revision.submitter"/></strong></td>
            <td>${revision.owner}</td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.format"/></strong></td>
            <td>${revision.format.name} <a href="${g.createLink(controller: 'model', action: 'downloadModelRevision', id: revision.identifier())}"><g:message code="model.summary.model.download"/></a></td>
        </tr>
        <tr>
            <td><strong><g:message code="model.summary.model.creators"/></strong></td>
            <td>
                <g:if test="${revision.model.creators.size() == 1}">
                    ${revision.model.creators.toList().first()}
                </g:if>
                <g:else>
                    <ul>
                        <g:each in="${revision.model.creators}">
                            <li>${it}</li>
                        </g:each>
                    </ul>
                </g:else>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="model-annotations">
    <h2><g:message code="model.summary.annotation"/></h2>
    <jummp:annotations annotations="${annotations}" model="false"/>
</div>
<g:if test="${notes}">
<div id="model-notes">
    <h2><g:message code="model.summary.notes"/></h2>
    <sbml:notes notes="${notes}"/>
</div>
</g:if>
