<div id="miriam">
    <form action="${g.createLink(controller: 'miriam', action: 'updateResources')}" method="POST">
        <table>
            <tr>
                <th><label for="miriam-update-miriam-url"><g:message code="miriam.update.ui.url"/></label></th>
                <td><span><input type="text" id="miriam-update-miriam-url" name="miriamUrl" value="http://www.ebi.ac.uk/miriam/main/export/xml/"/><jummp:errorField/></span></td>
            </tr>
            <tr>
                <th><label for="miriam-update-force"><g:message code="miriam.update.ui.force"/></label></th>
                <td><span><g:checkBox id="miriam-update-force" name="force"/><jummp:errorField/></span></td>
            </tr>
        </table>
        <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
            <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
            <input type="button" value="${g.message(code: 'ui.button.save')}"/>
        </div>
    </form>
</div>
<div id="miriam-update">
    <g:message code="miriam.data.update"/>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="button" value="${g.message(code: 'ui.button.schedule')}"/>
    </div>
</div>
<div id="miriam-model-update">
    <g:message code="miriam.model.update"/>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="button" value="${g.message(code: 'ui.button.schedule')}"/>
    </div>
</div>
