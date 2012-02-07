<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="brandingInternalColor">Internal
                        Color:</label></td>
                <td
                    class="value ${hasErrors(bean: branding, field: 'internalColor', 'errors')}">
                    <input type="text" name="internalColor"
                    id="brandingInternalColor"
                    value="${branding ? branding?.internalColor : ''}" />
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="brandingExternalColor">External
                        Color:</label></td>
                <td
                    class="value ${hasErrors(bean: branding, field: 'externalColor', 'errors')}">
                    <input type="text" name="externalColor"
                    id="brandingExternalColor"
                    value="${branding ? branding?.externalColor : ''}" />
                </td>
            </tr>
        </tbody>
    </table>
</div>
========================
<br />
${grailsApplication.config.jummp.branding.internalColor}
<br />
========================
