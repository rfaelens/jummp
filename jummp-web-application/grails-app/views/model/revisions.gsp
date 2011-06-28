<div id="model-revisions">
<table>
    <g:each var="revision" in="${revisions}" status="i">
        <tr>
            <td class="revisionNumber"><a href="#">${revision.revisionNumber}</a></td>
            <td><span class="revisionComment">${revision.comment}</span><br/>
                <span class="revisionUploadedBy"><g:message code="model.revision.uploadedBy" args="${[revision.uploadDate, revision.owner]}"/></span>
                <g:if test="${revision.minorRevision}"><span class="revisionMinor"><g:message code="model.revision.minor"/></span></g:if>
            </td>
            <td class="revisionControl">
                <g:if test="${i == 0}">
                    <form name="delete" accept="${g.createLink(controller: model, action: deleteRevision)}">
                        <input type="hidden" name="id" value="${revision.model.id}"/>
                        <input type="hidden" name="revision" value="${revision.revisionNumber}"/>
                    </form>
                    <a href="#" class="delete"><g:message code="model.revision.delete.button"/></a>
                </g:if>
            </td>
        </tr>
    </g:each>
</table>
</div>

<g:if test="${addRevision}">
    <div id="model-addrevision">
    <h2><g:message code="model.revision.upload.goto"/></h2>
    <form action="saveNewRevision" method="post" id="revision-upload-form" enctype="multipart/form-data">
        <input type="hidden" name="modelId" value="${params?.id}">
        <table>
            <thead></thead>
            <tbody>
            <tr>
                <td><label for="revision-upload-file"><g:message code="model.upload.file"/>:</label></td>
                <td><span><input type="file" id="revision-upload-file" name="model"/><jummp:errorField/></span></td>
            </tr>
            <tr>
                <td><label for="revision-upload-comment"><g:message code="model.upload.comment"/>:</label></td>
                <td><span><input type="text" id="revision-upload-comment" name="comment"><jummp:errorField/></span></td>
            </tr>
            </tbody>
        </table>
        <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
            <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
            <input type="button" value="${g.message(code: 'ui.button.upload')}"/>
        </div>
    </form>
    </div>
</g:if>
