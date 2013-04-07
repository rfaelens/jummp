<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="url" title="The servers production URL">URL:</label></td>
                <td class="value ${hasErrors(bean: server, field: 'url', 'errors')}">
                    <input type="text" name="url" id="url" value="${server ? server.url : 'http://127.0.0.1:8080/jummp/'}" title="The server's production URL"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="protectEverything"
                    title="Whether the complete application requires login">Protect all pages:</label></td>
                <td class="value ${hasErrors(bean: server, field: 'protectEverything', 'errors')}">
                    <g:checkBox name="protectEverything" id="protectEverything" value="${true}" checked="${server ? server.protectEverything : 'false'}" title="Whether the complete application requires login"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
