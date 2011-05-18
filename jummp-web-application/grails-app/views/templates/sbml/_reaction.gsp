<tr title="${title}">
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
