<%@ page contentType="text/html;charset=UTF-8" %>
<h1><g:message code="model.view.header" args="[revision.model.id, revision.model.name]"/></h1>
<a href="#" onclick="showModelList()">Back to Model List</a>
<div id="modelTabs" style="display: none">
    <ul>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Model</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Overview</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Math</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Physical Entities</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Parameters</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Curation</a></li>
    </ul>
</div>
