<table>
    <g:if test="${reaction.math}">
        <jummp:contentMathML mathML="${reaction.math}"/>
    </g:if>
    <jummp:sboTableRow sbo="${reaction.sboTerm}"/>
    <jummp:annotationsTableRow annotations="${reaction.annotation}"/>
    <sbml:notesTableRow notes="${reaction.notes}"/>
</table>
