<table>
    <jummp:contentMathMLTableRow mathML="${reaction.math}"/>
    <tr>
        <td class="reactionValue" colspan="2">
        <span class="reactionReactants">
        <g:each var="reactant" in="${reaction.reactants}" status="i">
            <g:if test="${reactant.stoichiometry != 1}">${reactant.stoichiometry} &#215;</g:if>
            [${reactant.speciesName ? reactant.speciesName : reactant.species}]
            <g:if test="${i != reaction.reactants.size() -1}">+</g:if>
        </g:each>
        </span>
        <span class="reactionArrow">${reaction.reversible ? '&harr;' : '&rarr;'}</span>
        <span class="reactionProducts">
        <g:each var="product" in="${reaction.products}" status="i">
            <g:if test="${product.stoichiometry != 1}">${product.stoichiometry} &#215;</g:if>
            [${product.speciesName ? product.speciesName : product.species}]
            <g:if test="${i != reaction.products.size() -1}">+</g:if>
        </g:each>
        </span>
        <span class="reactionProductEnd">;</span>
        <span class="reactionModifiers">
        <g:each var="modifier" in="${reaction.modifiers}" status="i">
            {${modifier.speciesName ? modifier.speciesName : modifier.species}}
            <g:if test="${i != reaction.modifiers.size() -1}">,</g:if>
        </g:each>
        </span>
        </td>
    </tr>
    <jummp:sboTableRow sbo="${reaction.sbo}"/>
    <jummp:annotationsTableRow annotations="${reaction.annotation}"/>
    <sbml:notesTableRow notes="${reaction.notes}"/>
</table>
