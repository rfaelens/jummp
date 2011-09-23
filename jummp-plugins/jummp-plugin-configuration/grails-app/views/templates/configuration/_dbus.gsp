<div class="dialog">
    <table class="formtable">
        <tbody>
        <tr class="prop">
                <td class="name"><label for="systemBus">Activate System DBus</label></td>
                <td class="value ${hasErrors(bean: dbus, field: 'dbus', 'errors')}">
                    <g:radio name="systemBus" value="true" checked="${dbus?.systemBus}"/>
                </td>
            </tr>
        <tr class="prop">
                <td class="name"><label for="systemBus">Activate Session D-Bus</label></td>
                <td class="value ${hasErrors(bean: dbus, field: 'dbus', 'errors')}">
                    <g:radio name="systemBus" value="false" checked="${dbus?.systemBus}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
