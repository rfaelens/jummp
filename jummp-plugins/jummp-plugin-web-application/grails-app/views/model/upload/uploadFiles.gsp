<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Upload Files</title>
        <style>
            .normalAnchor {
                color: #CCCC00;
            }
        </style>
    </head>
    <body>
        <g:hasErrors>
            <div class="errors">
                <g:renderErrors/>
                <g:if test="${flash.invalidToken}">
                    <g:message code="flash.invalidToken"/>
                </g:if>
            </div>
        </g:hasErrors>
        <h1><g:message code="submission.upload.header"/></h1>
        <p style="padding-bottom:1em"><g:message code="submission.upload.explanation"/></p>
        <g:uploadForm id="fileUpload" novalidate="false" autocomplete="false">
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="mainFile">
                                    <g:message code="submission.upload.mainFile.label"/>
                                </label>
                            </td>
                            <td class="value">
                                <input type="file" id="mainFile" name="mainFile"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <a href="#" id="addFile" class="normalAnchor"><g:message code="submission.upload.additionalFiles.addButton" /></a>
                <a href="#" id="removeFiles" class="normalAnchor"><g:message code="submission.upload.additionalFiles.removeAllButton" /></a>
                <fieldset>
                    <legend>
                        <g:message code="submission.upload.additionalFiles.legend"/>
                    </legend>
                    <table class='formtable' id="additionalFiles">
                        <tbody>
                        </tbody>
                    </table>
                </fieldset>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.upload.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.upload.backButton')}" />
                    <g:submitButton name="Upload" value="${g.message(code: 'submission.upload.uploadButton')}" />
                </div>
            </div>
        </g:uploadForm>
        <g:javascript>
            $(document).ready(function () {
                $('#removeFiles').click(function(e) {
                    e.preventDefault();
                    $('tr.fileEntry').empty();
                });

                $(document).on("click", 'a.killer', function (e) {
                    e.preventDefault();
                    $(this).parent().parent().empty();
                });

                $("#addFile").click(function (evt) {
                    evt.preventDefault();
                    $('<tr>', {
                        class: 'fileEntry'
                    }).append(
                        $('<td>').append(
                            $('<input>', {
                                type: 'file',
                                name: "extraFiles",
                            })
                        ),
                        $('<td>').append(
                            $('<input>', {
                                type: 'text',
                                name: "description",
                                placeholder: "Please enter a description"
                            })
                        ),
                        $('<td>').append(
                            $('<a>', {
                                href: "#",
                                class: 'killer normalAnchor',
                                text: 'Discard'
                            })
                        )
                    ).appendTo('table#additionalFiles');
                });

                $("#uploadButton").click( function() {
                    $("#fileUpload").submit();
                });

                $("#cancelButton").click( function() {
                    $("#fileUpload").reset();
                });
            });
        </g:javascript>
    </body>
</html>
