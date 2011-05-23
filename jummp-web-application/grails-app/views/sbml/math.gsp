<div id="model-math">
    <table>
        <sbml:renderReactions reactions="${reactions}"/>
        <sbml:renderRules rules="${rules}"/>
        <g:if test="${!events.isEmpty()}">
            <sbml:renderEvents events="${events}"/>
        </g:if>
        <sbml:renderFunctionDefinitions functions="${functions}"/>
    </table>
</div>
