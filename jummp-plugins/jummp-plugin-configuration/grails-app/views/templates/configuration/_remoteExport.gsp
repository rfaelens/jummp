<div class="dialog">
    <table class="formtable">
        <tbody>
        <tr class="prop">
            <td class="name"><label for="jummpExportJms">Activate JMS</label></td>
            <td class="value ${hasErrors(bean: remote, field: 'jummpExportJms', 'errors')}">
                <g:checkBox name="jummpExportJms" value="${remote ? remote.jummpExportJms : true}" />
            </td>
        </tr>
        <tr class="prop">
            <td class="name"><label for="jummpExportDbus">Activate D-Bus</label></td>
            <td class="value ${hasErrors(bean: remote, field: 'jummpExportDbus', 'errors')}">
                <g:checkBox name="jummpExportDbus" value="${remote ? remote.jummpExportDbus : false}" />
            </td>
        </tr>
        </tbody>
    </table>
</div>
