<div id="model-parameters">
    <table>
    <thead><tr><th colspan="2">Global Parameters (${parameters.size()})</th></tr></thead>
<g:each var="param" in="${parameters}">
    <%
        String name = param.name ? param.name : param.id
        String metaLink = g.createLink(controller: 'sbml', action: 'parameterMeta', params: [id: params.id, parameterId: param.id, revision: params.revision])
    %>
    <tbody>
    <tr rel="${metaLink}" title="${name}">
        <th class="parameterTitle">${name}</th>
        <td class="parameterValue">
            <p>
            <g:if test="${param.value}">Value: ${param.value}</g:if>
            <g:if test="${param.unit}"><span class="parameterUnit">(Units: ${param.unit})</span></g:if>
            </p>
            <g:if test="${param.constant}">
            <p class="parameterConstant">Constant</p>
            </g:if>
        </td>
    </tr>
    </tbody>
</g:each>
    </table>
</div>