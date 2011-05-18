<%@ page contentType="text/html;charset=UTF-8" %>
<h1><g:message code="model.view.header" args="[revision.model.id, revision.model.name]"/></h1>
<div id="navigationButtons">
    <a href="#"><g:message code="model.list.goto"/></a>
    <a href="#"><g:message code="model.upload.goto"/></a>
</div>
<div id="modelTabs" style="display: none">
    <ul>
        <li><a id="modelTabs-model" href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Model</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Overview</a></li>
        <li><a id="modelTabs-math" href="${g.createLink(controller: 'sbml', action: 'math', id: params.id)}?revision=${revision.revisionNumber}">Math</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Physical Entities</a></li>
        <li><a id="modelTabs-parameter" href="${g.createLink(controller: 'sbml', action: 'parameter', id: params.id)}?revision=${revision.revisionNumber}">Parameters</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Curation</a></li>
        <g:if test="${addRevision}">
            <li><a id="modelTabs-addRevision" href="${g.createLink(controller: 'model', action: 'newRevision', id: params.id)}"><g:message code="model.revision.upload.goto"/></a></li>
        </g:if>
    </ul>
</div>
