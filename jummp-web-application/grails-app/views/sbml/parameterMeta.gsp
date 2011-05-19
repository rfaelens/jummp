<table>
    <g:if test="${parameter.sboTerm}">
    <tr>
        <td>SBO:</td>
        <td>
<%
    String sboTerm = parameter.sboTerm
    while (sboTerm.size() != 7) {
        sboTerm = "0" + sboTerm
    }
%>
            <jummp:renderURN resource="urn:miriam:obo.sbo:SBO%3A${sboTerm}"/>
        </td>
    </tr>
    </g:if>
    <g:if test="${parameter.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${parameter.notes}"/></td>
    </tr>
    </g:if>
</table>
