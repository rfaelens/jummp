<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



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
