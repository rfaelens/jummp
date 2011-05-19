<table>
    <g:if test="${event.trigger}">
        <tr>
            <td>Trigger:</td>
            <td><jummp:contentMathML mathML="${event.trigger}"/></td>
        </tr>
    </g:if>
    <g:if test="${event.delay}">
        <tr>
            <td>Delay:</td>
            <td><jummp:contentMathML mathML="${event.delay}"/></td>
        </tr>
    </g:if>
    <jummp:sboTableRow sbo="${event.sboTerm}"/>
    <jummp:annotationsTableRow annotations="${event.annotation}"/>
    <g:if test="${event.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${event.notes}"/></td>
    </tr>
    </g:if>
</table>
