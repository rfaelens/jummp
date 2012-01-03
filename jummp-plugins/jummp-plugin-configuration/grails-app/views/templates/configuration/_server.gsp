<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="url" title="The servers production URL">URL:</label></td>
                <td class="value ${hasErrors(bean: server, field: 'url', 'errors')}">
                    <input type="text" name="url" id="url" value="${server ? server.url : 'http://127.0.0.1:8080/jummp/'}" title="The servers production URL"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="weburl" title="The web servers production URL">Web URL:</label></td>
                <td class="value ${hasErrors(bean: server, field: 'weburl', 'errors')}">
                    <input type="text" name="weburl" id="weburl" value="${server ? server.weburl : 'http://127.0.0.1:8080/jummp-web-application/'}" title="The web servers production URL"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
