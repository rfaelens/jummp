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
            <td><span><input type="file" id="model-upload-file" name="model"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-name"><g:message code="model.upload.name"/>:</label></td>
            <td><span><input type="text" id="model-upload-name" name="name"><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-comment"><g:message code="model.upload.comment"/>:</label></td>
            <td><span><input type="text" id="model-upload-comment" name="comment"><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td colspan="2"><g:message code="model.upload.publication.section"/></td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-pubmed" name="publicationType" value="PUBMED"/><label for="model-upload-publication-pubmed"><g:message code="model.upload.pubmed"/>:</label></td>
            <td><span><input type="text" id="model-upload-pubmed" name="pubmed"><jummp:errorField/></span> (<a href="http://www.ebi.ac.uk/citexplore/" target="_blank"><g:message code="publication.search.pubmed"/></a>)</td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-doi" name="publicationType" value="DOI"/><label for="model-upload-publication-doi"><g:message code="model.upload.doi"/>:</label></td>
            <td><span><input type="text" id="model-upload-doi" name="doi"><jummp:errorField/></span> (<a href="http://www.doi.org/" target="_blank"><g:message code="publication.search.doi"/></a>)</td>
        </tr>
        <tr>
            <td><input type="radio" id="model-upload-publication-url" name="publicationType" value="URL"/><label for="model-upload-publication-url"><g:message code="model.upload.url"/>:</label></td>
            <td><span><input type="text" id="model-upload-url" name="url"><jummp:errorField/></span></td>
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
            <td><label for="model-upload-publication-title"><g:message code="publication.title"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-title" name="publicationTitle"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-journal"><g:message code="publication.journal"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-journal" name="publicationJournal"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-issue"><g:message code="publication.issue"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-issue" name="publicationIssue"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-volume"><g:message code="publication.volume"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-volume" name="publicationVolume"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-pages"><g:message code="publication.pages"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-pages" name="publicationPages"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-affiliation"><g:message code="publication.affiliation"/>:</label></td>
            <td><span></span><input type="text" id="model-upload-publication-affiliation" name="publicationAffiliation"/><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-year"><g:message code="publication.date"/>:</label></td>
            <td>
                <span><select name="publicationYear" id="model-upload-publication-year">
                    <option value=""><g:message code="publication.upload.year"/></option>
<%
    for (int i=new GregorianCalendar().get(Calendar.YEAR); i>=1980; i--) {
%>
                    <option value="${i}">${i}</option>
<%
    }
%>
                </select><jummp:errorField/></span>
                <span><select name="publicationMonth" id="model-upload-publication-month">
                    <option value=""><g:message code="publication.upload.month"/></option>
                    <option value="Jan"><g:message code="date.month.january"/></option>
                    <option value="Feb"><g:message code="date.month.february"/></option>
                    <option value="Mar"><g:message code="date.month.march"/></option>
                    <option value="Apr"><g:message code="date.month.april"/></option>
                    <option value="May"><g:message code="date.month.may"/></option>
                    <option value="Jun"><g:message code="date.month.june"/></option>
                    <option value="Jul"><g:message code="date.month.july"/></option>
                    <option value="Aug"><g:message code="date.month.august"/></option>
                    <option value="Sep"><g:message code="date.month.september"/></option>
                    <option value="Oct"><g:message code="date.month.october"/></option>
                    <option value="Nov"><g:message code="date.month.november"/></option>
                    <option value="Dec"><g:message code="date.month.december"/></option>
                </select><jummp:errorField/></span>
                <span><select name="publicationDay" id="model-upload-publication-day">
                    <option value=""><g:message code="publication.upload.day"/></option>
<%
    for (int i=1; i<=31; i++) {
%>
                    <option value="${i}">${i}</option>
<%
    }
%>
                </select><jummp:errorField/></span>
            </td>
        </tr>
        <tr>
            <td><label for="model-upload-publication-abstract"><g:message code="publication.abstract"/>:</label></td>
            <td><span><textarea rows="4" cols="20" maxlength="1000" id="model-upload-publication-abstract" name="publicationAbstract"></textarea><jummp:errorField/></span></td>
        </tr>
        <tr>
            <td colspan="2"><g:message code="publication.upload.authors"/><input id="model-upload-publication-author-add" type="button" value="${g.message(code: 'publication.upload.ui.addAuthor')}"/></td>
        </tr>
        <tr id="model-upload-publication-author-initials-row">
            <td><label for="model-upload-publication-author-initials"><g:message code="publication.upload.author.initials"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-author-initials" name="authorInitials" size="5" maxlength="5"/><jummp:errorField/></span><input type="button" style="display:none" value="${g.message(code: 'publication.upload.ui.removeAuthor')}"/></td>
        </tr>
        <tr id="model-upload-publication-author-firstname-row">
            %{--TODO preset fields with users data --}%
            <td><label for="model-upload-publication-author-firstname"><g:message code="publication.upload.author.firstName"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-author-firstname" name="authorFirstName"/><jummp:errorField/></span></td>
        </tr>
        <tr id="model-upload-publication-author-lastname-row">
            <td><label for="model-upload-publication-author-lastname"><g:message code="publication.upload.author.lastName"/>:</label></td>
            <td><span><input type="text" id="model-upload-publication-author-lastname" name="authorLastName"/><jummp:errorField/></span></td>
        </tr>
        </tbody>
    </table>
    <input type="hidden" id="model-upload-author-count" value="0" name="authorCount"/>
    <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
        <input type="reset" value="${g.message(code: 'ui.button.cancel')}"/>
        <input type="button" value="${g.message(code: 'ui.button.upload')}" onclick="uploadModel()"/>
    </div>
</form>
