<%@ page import="net.biomodels.jummp.core.model.PublicationLinkProvider" %>
<div id="model-reference-publication">
    <g:if test="${publication.link && publication.linkProvider}">
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
            <td>
<%
    String publicationSummary = publication.journal
    if (publication.year) {
        publicationSummary += " " + publication.year
        if (publication.month) {
            publicationSummary += " " + publication.month
        }
    }
    if (publication.volume) {
        publicationSummary += "; " + publication.volume
    }
    if (publication.issue) {
        publicationSummary += "(" + publication.issue + ")"
    }
    if (publication.pages) {
        publicationSummary += ": " + publication.pages
    }
%>
                ${publicationSummary}
            </td>
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
</div>
