<%@ page contentType="text/html;charset=UTF-8" %>
<h1 id="model-header"><span>${revision.model.id}</span><span>${revision.model.name}</span><span>${revision.revisionNumber}</span></h1>
<div id="modelTabs" style="display: none">
    <ul>
        <li><a id="modelTabs-model" href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}?revision=${revision.revisionNumber}">Model</a></li>
        <li><a id="modelTabs-overview" href="${g.createLink(controller: 'sbml', action: 'overview', id: params.id)}?revision=${revision.revisionNumber}">Overview</a></li>
        <li><a id="modelTabs-math" href="${g.createLink(controller: 'sbml', action: 'math', id: params.id)}?revision=${revision.revisionNumber}">Math</a></li>
        <li><a id="modelTabs-entity" href="${g.createLink(controller: 'sbml', action: 'entity', id: params.id)}?revision=${revision.revisionNumber}">Physical Entities</a></li>
        <li><a id="modelTabs-parameter" href="${g.createLink(controller: 'sbml', action: 'parameter', id: params.id)}?revision=${revision.revisionNumber}">Parameters</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Curation</a></li>
        <li><a id="modelTabs-revisions" href="${g.createLink(controller: 'model', action: 'revisions', id: params.id)}">Revisions</a></li>
        <g:if test="${addRevision}">
            <li><a id="modelTabs-addRevision" href="${g.createLink(controller: 'model', action: 'newRevision', id: params.id)}"><g:message code="model.revision.upload.goto"/></a></li>
        </g:if>
    </ul>
</div>
