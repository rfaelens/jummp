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
<div class="dialog">
    <table class="formtable">
        <tbody>
        <tr class="prop">
                <td class="name"><label for="jummpRemote">Activate JMS</label></td>
                <td class="value ${hasErrors(bean: remote, field: 'remoteRemote', 'errors')}">
                    <g:radio name="jummpRemote" value="jms" checked="${remote ? remote.jummpRemote == 'jms' : true}"/>
                </td>
            </tr>
        <tr class="prop">
                <td class="name"><label for="jummpRemote">Activate D-Bus</label></td>
                <td class="value ${hasErrors(bean: remote, field: 'remoteRemote', 'errors')}">
                    <g:radio name="jummpRemote" value="dbus" checked="${remote ? remote.jummpRemote == 'dbus' : false}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
