<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="startRemoveOffset" title="Offset before trigger initially runs">Start remove offset:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'startRemoveOffset', 'errors')}">
                    <input type="text" name="startRemoveOffset" id="startRemoveOffset" value="${trigger?.startRemoveOffset ? trigger.startRemoveOffset : "300000"}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="removeInterval" title="Remove interval of the trigger">Remove interval:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'removeInterval', 'errors')}">
                    <input type="text" name="removeInterval" id="removeInterval" value="${trigger?.removeInterval ? trigger?.removeInterval : "1800000"}"/>
                </td>
            </tr>
        <tr class="prop">
                <td class="name"><label for="maxInactiveTime" title="The maximum inactive interval of users">Maximum inactive time:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'maxInactiveTime', 'errors')}">
                    <input type="text" name="maxInactiveTime" id="maxInactiveTime" value="${trigger?.maxInactiveTime ? trigger?.maxInactiveTime : "1800000"}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
