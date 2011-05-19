<tr rel="${metaLink}" title="${title}">
    <th class="eventTitle">${title}</th>
    <td class="eventValue">
        <g:if test="${assignments.size() == 1}">
            <sbml:renderEventAssignment assignment="${assignments[0]}"/>
        </g:if>
        <g:else>
            <ul>
            <g:each var="assignment" in="${assignments}">
                <li><sbml:renderEventAssignment assignment="${assignment}"/></li>
            </g:each>
            </ul>
        </g:else>
    </td>
</tr>
