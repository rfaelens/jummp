<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="bivesDiffDirectory">Diff directory:</label></td>
                <td class="value ${hasErrors(bean: bives, field: 'diffdir', 'errors')}">
                    <input type="text" name="diffDir" id="bivesDiffDirectory" value="${bives ? bives.diffDir}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
