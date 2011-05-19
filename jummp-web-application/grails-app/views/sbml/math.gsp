<div id="model-math">
    <table>
        <sbml:renderReactions reactions="${reactions}"/>
        <g:if test="${!events.isEmpty()}">
            <sbml:renderEvents events="${events}"/>
        </g:if>
    </table>
</div>
