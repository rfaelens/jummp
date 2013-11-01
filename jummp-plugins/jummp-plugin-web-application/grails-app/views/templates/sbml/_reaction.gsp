<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>











<tr rel="${metaLink}" title="${title}">
    <th class="reactionTitle">${title}</th>
    <td class="reactionValue">
        <span class="reactionReactants">
        <g:each var="reactant" in="${reactants}" status="i">
            <g:if test="${reactant.stoichiometry != 1}">${reactant.stoichiometry} &#215;</g:if>
            [${reactant.speciesName ? reactant.speciesName : reactant.species}]
            <g:if test="${i != reactants.size() -1}">+</g:if> 
        </g:each>
        </span>
        <span class="reactionArrow">${reversible ? '&harr;' : '&rarr;'}</span>
        <span class="reactionProducts">
        <g:each var="product" in="${products}" status="i">
            <g:if test="${product.stoichiometry != 1}">${product.stoichiometry} &#215;</g:if>
            [${product.speciesName ? product.speciesName : product.species}]
            <g:if test="${i != products.size() -1}">+</g:if> 
        </g:each>
        </span>
        <span class="reactionProductEnd">;</span>
        <span class="reactionModifiers">
        <g:each var="modifier" in="${modifiers}" status="i">
            {${modifier.speciesName ? modifier.speciesName : modifier.species}}
            <g:if test="${i != modifiers.size() -1}">,</g:if>
        </g:each>
        </span>
    </td>
</tr>
