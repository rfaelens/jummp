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
