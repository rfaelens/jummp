<table>
    <jummp:sboTableRow sbo="${parameter.sboTerm}"/>
    <g:if test="${parameter.annotation}">
    <tr>
        <td>Annotations:</td>
        <td><jummp:annotations annotations="${parameter.annotation}" model="false"/></td>
    </tr>
    </g:if>
    <g:if test="${parameter.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${parameter.notes}"/></td>
    </tr>
    </g:if>
</table>
