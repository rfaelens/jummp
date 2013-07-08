<div class="dialog">
    <table class="formtable">
        <tbody>
        <tr class="prop">
            <td class="name"><label for="jummpExportJms">Activate JMS</label></td>
            <td class="value ${hasErrors(bean: remote, field: 'jummpExportJms', 'errors')}">
                <g:checkBox name="jummpExportJms" value="${remote ? remote.jummpExportJms : true}" />
            </td>
        </tr>
        </tbody>
    </table>
</div>
