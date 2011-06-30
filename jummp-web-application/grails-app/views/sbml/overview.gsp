<div id="model-math">
    <h2><g:message code="model.overview.math"/></h2>
    <table>
        <sbml:renderReactionsOverview reactions="${reactions}"/>
        <sbml:renderRulesOverview rules="${rules}"/>
    </table>
</div>
<div id="model-entity">
    <h2><g:message code="model.overview.entities"/></h2>
    <table>
        <sbml:renderCompartmentsOverview compartments ="${compartments}"/>
    </table>
</div>
<div id="model-parameters">
    <h2><g:message code="model.overview.parameters"/></h2>
    <table>
    <sbml:renderParametersOverview title="${g.message(code: 'sbml.parameters.globalParameters')}" parameters="${parameters}"/>
<g:each var="reaction" in="${reactionParameters}">
    <g:if test="${!reaction.parameters.isEmpty()}">
        <sbml:renderParameters title="${reaction.name ? reaction.name : reaction.id}" parameters="${reaction.parameters}"/>
    </g:if>
</g:each>
    </table>
</div>
