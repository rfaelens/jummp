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


<%@ page import="net.biomodels.jummp.model.PublicationLinkProvider" %>
<g:if test="${publication?.link && publication?.linkProvider}">
<h2><g:message code="model.summary.reference-publication"/></h2>
<table>
    <thead></thead>
    <tbody>
    <g:if test="${publication.linkProvider == PublicationLinkProvider.PUBMED}">
        <tr>
            <td><strong><g:message code="publication.pubmedid"/></strong></td>
            %{-- TODO link should not be static--}%
            <td><a target="_blank" href="http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=${publication.link}">${publication.link}</a></td>
        </tr>
    </g:if>
    <g:if test="${publication.linkProvider == PublicationLinkProvider.DOI}">
        <tr>
            <td><strong><g:message code="publication.doi"/></strong></td>
            %{-- TODO link should not be static--}%
            <td><a target="_blank" href="http://dx.doi.org/${publication.link}">${publication.link}</a></td>
        </tr>
    </g:if>
    <tr>
        <td><strong><g:message code="publication.journal"/></strong></td>
        <td><jummp:compactPublicationTitle publication="${publication}"/></td>
    </tr>
    <tr>
        <td><strong><g:message code="publication.title"/></strong></td>
        <td>${publication.title}</td>
    </tr>
    <tr>
        <td><strong><g:message code="publication.authors"/></strong></td>
        <td>
<%
String authors = ""
publication.authors.eachWithIndex { author, i ->
    authors += author.initials + " " + author.lastName
    if (i < publication.authors.size() - 1) {
        authors += ", "
    }
}
%>
            ${authors}
        </td>
    </tr>
    <g:if test="${publication.affiliation}">
        <tr>
            <td><strong><g:message code="publication.affiliation"/></strong></td>
            <td>${publication.affiliation}</td>
        </tr>
    </g:if>
    <g:if test="${publication.synopsis}">
        <tr>
            <td><strong><g:message code="publication.abstract"/></strong></td>
            <td>
<%
if (publication.synopsis.size() > 100) {
%>
    ${publication.synopsis.substring(0, 99)}<a href="#" onclick="$('#model-reference-publication-abstract').dialog();">…</a>
    <div id="model-reference-publication-abstract" title="${g.message(code: 'publication.abstract')}" style="display: none">
        ${publication.synopsis}
    </div>
<%
} else {
%>
    ${publication.synopsis}
<%
}
%>
            </td>
        </tr>
    </g:if>
    </tbody>
</table>
</g:if>
<g:else>
    <h2><g:message code="model.summary.reference-publication-unpublished"/></h2>
</g:else>
