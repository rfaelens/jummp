<table>
    <tr>
    <td class="compartmentValue">
    <g:if test="${compartment.spatialDimensions}">
        <tr>
            <g:message code="smbl.compartments.spatialDimensions" args="${[compartment.spatialDimensions]}"/>
        </tr>
    </g:if>
    <g:if test="${compartment.size}">&nbsp;
        <tr>
            <td><g:message code="sbml.compartments.size" args="${[compartment.size]}"/></td>
        </tr>
    </g:if>
    <jummp:sboTableRow sbo="${compartment.sbo}"/>
    <jummp:annotationsTableRow annotations="${compartment.annotation}"/>
    <sbml:notesTableRow notes="${compartment.notes}"/>
    <g:if test="${compartment.allSpecies.size() == 1}">
            <sbml:renderSpeciesOverview species="${compartment.allSpecies[0]}"/>
        </g:if>
        <g:else>
            <ul>
            <g:each var="species" in="${compartment.allSpecies}">
                <li><sbml:renderSpeciesOverview species="${species}"/></li>
            </g:each>
            </ul>
        </g:else>
</table>
