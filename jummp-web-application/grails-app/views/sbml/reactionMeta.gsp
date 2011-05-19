<table>
    <g:if test="${reaction.math}">
        <jummp:contentMathML mathML="${reaction.math}"/>
    </g:if>
    <jummp:sboTableRow sbo="${reaction.sboTerm}"/>
    <jummp:annotationsTableRow annotations="${reaction.annotation}"/>
    <g:if test="${reaction.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${reaction.notes}"/></td>
    </tr>
    </g:if>
</table>
