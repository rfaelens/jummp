<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="bivesDiffDirectory">Diff directory:</label></td>
                <td class="value ${hasErrors(bean: bives, field: 'diffDir', 'errors')}">
                    <input type="text" name="diffDir" id="bivesDiffDirectory" value="${bives?.diffDir}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
