<table>
    <jummp:sboTableRow sbo="${parameter.sboTerm}"/>
    <jummp:annotationsTableRow annotations="${parameter.annotation}"/>
    <g:if test="${parameter.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${parameter.notes}"/></td>
    </tr>
    </g:if>
</table>
