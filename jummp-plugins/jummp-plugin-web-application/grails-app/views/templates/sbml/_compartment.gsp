<tr rel="${metaLink}" title="${title}">
    <th class="compartmentTitle">${title}</th>
    <td class="compartmentValue">
        <g:if test="${spatialDimensions}">
            <g:message code="smbl.compartments.spatialDimensions" args="${[spatialDimensions]}"/>
        </g:if>
        <g:if test="${size}">&nbsp;
            <g:message code="sbml.compartments.size" args="${[size]}"/>
        </g:if>
        <g:if test="${allSpecies.size() == 1}">
            <sbml:renderSpecies species="${allSpecies[0]}"/>
        </g:if>
        <g:else>
            <ul>
            <g:each var="species" in="${allSpecies}">
                <li><sbml:renderSpecies species="${species}"/></li>
            </g:each>
            </ul>
        </g:else>
    </td>
</tr>
