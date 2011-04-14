<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="create">Create Admin User:</label></td>
                <td class="value ${hasErrors(bean: firstRun, field: 'firstRun', 'errors')}"><input type="radio" name="firstRun" id="create" value="true" checked="checked"/></td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="reuse">Reuse existing user in Database:</label></td>
                <td class="value ${hasErrors(bean: firstRun, field: 'firstRun', 'errors')}"><input type="radio" name="firstRun" id="reuse" value="false"/></td>
            </tr>
        </tbody>
    </table>
</div>
