<div id="model-reaction-graph">
    <h2><g:message code="model.reactiongraph.title"/></h2>
    <object data="${g.createLink(controller: 'sbml', action: 'reactionGraphSvg', id: model)}?revision=${revision}" type="image/svg+xml" width="100%" height="500">
        %{--For the useful browsers (MSIE) as a fallback--}%
        <img src="${g.createLink(controller: 'sbml', action: 'reactionGraphSvg', id: model)}?revision=${revision}" alt="Reaction Graph" width="100%" height="500"/>
    </object>
</div>
