<table>
<tr rel="${metaLink}" title="${title}">
    <th class="speciesTitle"><g:message code="smbl.species.title" args="${[title]}"/></th>
    <td class="speciesValue">
        <g:if test="${initialAmount.toString() != 'null'}">
            &nbsp;
            <g:message code="smbl.species.initialAmount" args="${[initialAmount]}"/>
        </g:if>
        <g:if test="${initialConcentration.toString() != 'null'}">
            &nbsp;
            <g:message code="smbl.species.initialConcentration" args="${[initialConcentration]}"/>
        </g:if>
        <g:if test="${substanceUnits}">
            &nbsp;
            <g:message code="smbl.species.substanceUnits" args="${[substanceUnits]}"/>
        </g:if>
    </td>
</tr>
</table>
