<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="localRepository" title="The repository to checkout from and commit to">Local Repository Path:</label></td>
                <td class="value ${hasErrors(bean: svn, field: 'localRepository', 'errors')}">
                    <input type="text" name="localRepository" id="localRepository" value="${svn?.localRepository}" title="Local Repository Path - The repository to checkout from and commit to"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
