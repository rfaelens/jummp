<tr rel="${metaLink}" title="${title}">
    <th class="compartmentTitle">${title}</th>
    <td class="compartmentValue">
    </td>
    <td class="species">
        <g:if test="${allSpecies.size() == 1}">
            ${allSpecies[0].id}
        </g:if>
        <g:else>
            <ul>
            <g:each var="species" in="${allSpecies}">
                ${species.id}
            </g:each>
            </ul>
        </g:else>
    </td>
</tr>
