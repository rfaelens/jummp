<%@ page contentType="text/html;charset=UTF-8" %>
<h1 id="model-header"><span>${revision.model.id}</span><span>${revision.model.name}</span><span>${revision.revisionNumber}</span></h1>
<div id="modelTabs" style="display: none">
    <ul>
        <li><a id="modelTabs-model" href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}?revision=${revision.revisionNumber}">Model</a></li>
        <li><a id="modelTabs-overview" href="${g.createLink(controller: 'sbml', action: 'overview', id: params.id)}?revision=${revision.revisionNumber}">Overview</a></li>
        <li><a id="modelTabs-math" href="${g.createLink(controller: 'sbml', action: 'math', id: params.id)}?revision=${revision.revisionNumber}">Math</a></li>
        <li><a id="modelTabs-entity" href="${g.createLink(controller: 'sbml', action: 'entity', id: params.id)}?revision=${revision.revisionNumber}">Physical Entities</a></li>
        <li><a id="modelTabs-parameter" href="${g.createLink(controller: 'sbml', action: 'parameter', id: params.id)}?revision=${revision.revisionNumber}">Parameters</a></li>
        <li><a id="modelTabs-reactionGraph" href="${g.createLink(controller: 'model', action: 'reactionGraph', id: params.id)}?revision=${revision.revisionNumber}">Reaction Graph</a></li>
        <li><a id="modelTabs-reactionOctave" href="${g.createLink(controller: 'sbml', action: 'reactionOctave', id: params.id)}?revision=${revision.revisionNumber}">Octave</a></li>
        <li><a href="${g.createLink(controller: 'model', action: 'summary', id: params.id)}">Curation</a></li>
        <li><a id="modelTabs-revisions" href="${g.createLink(controller: 'model', action: 'revisions', id: params.id)}">Revisions</a></li>
    </ul>
</div>
