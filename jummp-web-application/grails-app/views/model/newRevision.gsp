<%@ page contentType="text/html;charset=UTF-8" %>
<form action="saveNewRevision" method="post" id="revision-upload-form" enctype="multipart/form-data">
    <input type="hidden" name="modelId" value="${params?.id}">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="revision-upload-file"><g:message code="model.upload.file"/>:</label></td>
            <td><input type="file" id="revision-upload-file" name="model"/></td>
        </tr>
        <tr>
            <td><label for="revision-upload-comment"><g:message code="model.upload.comment"/>:</label></td>
            <td><input type="text" id="revision-upload-comment" name="comment"></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="button" value="Upload" onclick="uploadRevision()"/>
    </div>
</form>
