<table>
    <p>
            <g:if test="${parameter.value}">
                <g:message code="sbml.parameters.value" args="${[parameter.value]}"/>
                <g:if test="${parameter.unit}">&nbsp;<g:message code="smbl.parameters.unit" args="${[parameter.unit]}"/></g:if>
            </g:if>
        </p>
        <g:if test="${parameter.constant}">
            <p class="parameterConstant"><g:message code="sbml.parameters.constant"/></p>
        </g:if>
    <jummp:sboTableRow sbo="${parameter.sboTerm}" name="${parameter.sboName}"/>
    <jummp:annotationsTableRow annotations="${parameter.annotation}"/>
    <sbml:notesTableRow notes="${parameter.notes}"/>
</table>
