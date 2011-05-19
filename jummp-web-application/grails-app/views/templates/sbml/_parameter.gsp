<tr rel="${metaLink}" title="${title}">
    <th class="parameterTitle">${title}</th>
    <td class="parameterValue">
        <p>
            <g:if test="${value}">
                <g:message code="sbml.parameters.value" args="${[value]}"/>
                <g:if test="${unit}">&nbsp;<g:message code="smbl.parameters.unit" args="${[unit]}"/></g:if>
            </g:if>
        </p>
        <g:if test="${constant}">
            <p class="parameterConstant"><g:message code="sbml.parameters.constant"/></p>
        </g:if>
    </td>
</tr>
