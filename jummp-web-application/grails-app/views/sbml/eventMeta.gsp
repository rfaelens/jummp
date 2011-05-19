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
    <g:if test="${event.sboTerm}">
    <tr>
        <td>SBO:</td>
        <td>
<%
    String sboTerm = event.sboTerm
    while (sboTerm.size() != 7) {
        sboTerm = "0" + sboTerm
    }
%>
            <jummp:renderURN resource="urn:miriam:obo.sbo:SBO%3A${sboTerm}"/>
        </td>
    </tr>
    </g:if>
    <g:if test="${event.annotation}">
    <tr>
        <td>Annotations:</td>
        <td><jummp:annotations annotations="${event.annotation}" model="false"/></td>
    </tr>
    </g:if>
    <g:if test="${event.notes}">
    <tr>
        <td>Notes:</td>
        <td><sbml:notes notes="${event.notes}"/></td>
    </tr>
    </g:if>
</table>
