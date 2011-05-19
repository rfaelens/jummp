<table>
    <g:if test="${reaction.math}">
        <jummp:contentMathML mathML="${reaction.math}"/>
    </g:if>
    <jummp:sboTableRow sbo="${reaction.sboTerm}"/>
    <g:if test="${reaction.annotation}">
    <tr>
        <td>Annotations:</td>
        <td><jummp:annotations annotations="${reaction.annotation}" model="false"/></td>
    </tr>
    </g:if>
    <g:if test="${reaction.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${reaction.notes}"/></td>
    </tr>
    </g:if>
</table>
