<div id="model-parameters">
    <table>
    <sbml:renderParameters title="${g.message(code: 'sbml.parameters.globalParameters')}" parameters="${parameters}"/>
<g:each var="reaction" in="${reactionParameters}">
    <g:if test="${!reaction.parameters.isEmpty()}">
        <sbml:renderParameters title="${reaction.name ? reaction.name : reaction.id}" parameters="${reaction.parameters}"/>
    </g:if>
</g:each>
    </table>
</div>
