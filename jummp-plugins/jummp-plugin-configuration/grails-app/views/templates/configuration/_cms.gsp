<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="cmsPolicyFile">CMS policy dir:</label></td>
                <td class="value ${hasErrors(bean: cms, field: 'policyFile', 'errors')}">
                    <input type="text" name="policyFile" id="cmsPolicyFile" value="${cms?.policyFile}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
