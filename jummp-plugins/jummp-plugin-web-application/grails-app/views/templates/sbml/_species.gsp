<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



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
