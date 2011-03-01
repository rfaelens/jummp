<%@ page contentType="text/html;charset=UTF-8" %>
<div id="navigationButtons">
    <a href="#" onclick="showModelList()"><g:message code="model.list.goto"/></a>
</div>
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
            <td><input type="text" id="model-upload-publication-title" name="publicationTitle"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-journal">Publication Journal:</label></td>
            <td><input type="text" id="model-upload-publication-journal" name="publicationJournal"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-issue">Publication Issue:</label></td>
            <td><input type="text" id="model-upload-publication-issue" name="publicationIssue"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-volume">Publication Volume:</label></td>
            <td><input type="text" id="model-upload-publication-volume" name="publicationVolume"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-pages">Publication Pages:</label></td>
            <td><input type="text" id="model-upload-publication-pages" name="publicationPages"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-affiliation">Publication Affiliation:</label></td>
            <td><input type="text" id="model-upload-publication-affiliation" name="publicationAffiliation"/></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-year">Publication Date:</label></td>
            <td>
                <select name="publicationYear" id="model-upload-publication-year">
                    <option value="">Select Year</option>
<%
    for (int i=new GregorianCalendar().get(Calendar.YEAR); i>=1980; i--) {
%>
                    <option value="${i}">${i}</option>
<%
    }
%>
                </select>
                <select name="publicationMonth" id="model-upload-publication-month">
                    <option value="">Select Month (optional)</option>
                    <option value="Jan">Jan</option>
                    <option value="Feb">Feb</option>
                    <option value="Mar">Mar</option>
                    <option value="Apr">Apr</option>
                    <option value="May">May</option>
                    <option value="Jun">Jun</option>
                    <option value="Jul">Jul</option>
                    <option value="Aug">Aug</option>
                    <option value="Sep">Sep</option>
                    <option value="Oct">Oct</option>
                    <option value="Nov">Nov</option>
                    <option value="Dec">Dec</option>
                </select>
                <select name="publicationDay" id="model-upload-publication-day">
                    <option value="">Select Day (optional)</option>
<%
    for (int i=1; i<=31; i++) {
%>
                    <option value="${i}">${i}</option>
<%
    }
%>
                </select>
            </td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-abstract">Publication Abstract:</label></td>
            <td><textarea rows="4" cols="20" maxlength="1000" id="model-upload-publication-abstract" name="publicationAbstract"></textarea></td>
        </tr>
        </tbody>
    </table>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="button" value="Upload" onclick="uploadModel()"/>
    </div>
</form>
