<div class="dialog">
    <table class="formtable">
        <tbody>
        <tr class="prop">
                <td class="name"><label for="validation">Enable SBML Validation</label></td>
                <td class="value ${hasErrors(bean: sbml, field: 'validation', 'errors')}">
                    <g:radio name="validation" value="true" checked="${sbml?.validation}"/>
                </td>
            </tr>
        <tr class="prop">
                <td class="name"><label for="validation">Disable SBML Validation</label></td>
                <td class="value ${hasErrors(bean: sbml, field: 'validation', 'errors')}">
                    <g:radio name="validation" value="false" checked="${sbml?.validation}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
