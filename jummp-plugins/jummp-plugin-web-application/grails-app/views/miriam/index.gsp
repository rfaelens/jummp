<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title><g:message code="miriam.title"/></title>
        <meta name="layout" content="main" />
        <r:require module="miriamAdministration"/>
    </head>
    <body>
        <h1><g:message code="miriam.title"/></h1>
        <div id="miriam">
            <form method="POST">
                <table>
                    <tr>
                        <th><label for="miriam-update-miriam-url"><g:message code="miriam.update.ui.url"/></label></th>
                        <td><input type="text" id="miriam-update-miriam-url" name="miriamUrl" value="http://www.ebi.ac.uk/miriam/main/export/xml/"/></td>
                    </tr>
                    <tr>
                        <th><label for="miriam-update-force"><g:message code="miriam.update.ui.force"/></label></th>
                        <td><g:checkBox id="miriam-update-force" name="force"/></td>
                    </tr>
                </table>
                <div class="buttons">
                    <input type="reset" value="${g.message(code: 'miriam.button.cancel')}"/>
                    <input type="submit" value="${g.message(code: 'miriam.button.save')}"/>
                </div>
            </form>
        </div>
        <div id="miriam-update">
            <form method="POST">
            <g:message code="miriam.data.update"/>
            <div class="buttons">
                <input type="submit" value="${g.message(code: 'miriam.button.schedule')}"/>
            </div>
            </form>
        </div>
        <div id="miriam-model-update">
            <form method="POST">
            <g:message code="miriam.model.update"/>
            <div class="buttons">
                <input type="submit" value="${g.message(code: 'miriam.button.schedule')}"/>
            </div>
            </form>
        </div>
        <r:script>
$(function() {
    $.jummp.miriam.init();
});
        </r:script>
    </body>
</html>
