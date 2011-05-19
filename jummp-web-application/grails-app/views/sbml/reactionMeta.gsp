<table>
    <g:if test="${reaction.math}">
        <jummp:contentMathML mathML="${reaction.math}"/>
    </g:if>
    <g:if test="${reaction.sboTerm}">
    <tr>
        <td>SBO:</td>
        <td>
<%
    String sboTerm = reaction.sboTerm
    while (sboTerm.size() != 7) {
        sboTerm = "0" + sboTerm
    }
%>
            <jummp:renderURN resource="urn:miriam:obo.sbo:SBO%3A${sboTerm}"/>
        </td>
    </tr>
    </g:if>
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
