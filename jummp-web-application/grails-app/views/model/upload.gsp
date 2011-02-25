<%@ page contentType="text/html;charset=UTF-8" %>
<form action="save" method="post" id="model-upload-form" class="ui-widget-content" enctype="multipart/form-data">
    <table>
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="model-upload-file"><g:message code="model.upload.file"/>:</label></td>
            <td><input type="file" id="model-upload-file" name="model"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-name"><g:message code="model.upload.name"/>:</label></td>
            <td><input type="text" id="model-upload-name" name="name"></td>
        </tr>
        <tr>
            <td><label for="model-upload-comment"><g:message code="model.upload.comment"/>:</label></td>
            <td><input type="text" id="model-upload-comment" name="comment"></td>
        </tr>
        <tr>
            <td colspan="2"><g:message code="model.upload.publication.section"/></td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-pubmed" name="publicationType" value="PUBMED"/><label for="model-upload-publication-pubmed"><g:message code="model.upload.pubmed"/>:</label></td>
            <td><input type="text" id="model-upload-pubmed" name="pubmed"> (<a href="http://www.ebi.ac.uk/citexplore/" target="_blank"><g:message code="publication.search.pubmed"/></a>)</td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-doi" name="publicationType" value="DOI"/><label for="model-upload-publication-doi"><g:message code="model.upload.doi"/>:</label></td>
            <td><input type="text" id="model-upload-doi" name="doi"> (<a href="http://www.doi.org/" target="_blank"><g:message code="publication.search.doi"/></a>)</td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-url" name="publicationType" value="URL"/><label for="model-upload-publication-url"><g:message code="model.upload.url"/>:</label></td>
            <td><input type="text" id="model-upload-url" name="url"></td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-unpublished" name="publicationType" value="UNPUBLISHED"/><label for="model-upload-publication-unpublished"><g:message code="model.upload.unpublished"/></label></td>
            <td>&nbsp;</td>
        </tr>
        </tbody>
    </table>
    <table id="model-upload-publication-table" style="display: none">
        <thead></thead>
        <tbody>
        <tr>
            <td><label for="model-upload-publication-title">Publication Title:</label></td>
            <td><input type="text" id="model-upload-publication-title" name="publication-title"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-journal">Publication Journal:</label></td>
            <td><input type="text" id="model-upload-publication-journal" name="publication-journal"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-issue">Publication Issue:</label></td>
            <td><input type="text" id="model-upload-publication-issue" name="publication-issue"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-volume">Publication Volume:</label></td>
            <td><input type="text" id="model-upload-publication-volume" name="publication-volume"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-pages">Publication Pages:</label></td>
            <td><input type="text" id="model-upload-publication-pages" name="publication-pages"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-affiliation">Publication Affiliation:</label></td>
            <td><input type="text" id="model-upload-publication-affiliation" name="publication-affiliation"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-year">Publication Date:</label></td>
            <td>
                %{--TODO: calendar object--}%
                <input type="text" id="model-upload-publication-year" size="4" maxlength="4" name="publication-journal"/>
            </td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="button" value="Upload" onclick="uploadModel()"/>
    </div>
</form>
