<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Upload Files</title>
        <style>
            .normalAnchor {
                color: #CCCC00
            }
        </style>
    </head>
    <body>
        <h1>Upload Files</h1>
        <p style="padding-bottom:1em">Can I has files plz?</p>
        <g:uploadForm id="fileUpload" useToken="true" novalidate="false" autocomplete="false">
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="mainFile">Main Submission File</label>
                            </td>
                            <td class="value">
                                <input type="file" id="mainFile" name="mainFile"/>
                            </td>
                        </tr>
                    </tbody>
                </table>
                <a href="#" id="addFile" class="normalAnchor">Add a supplementary file</a>
                <a href="#" id="removeFiles" class="normalAnchor">Remove all supplementary files</a>
                <fieldset>
                    <legend>Additional files</legend>
                    <table class='formtable'>
                        <tbody>
                            <tr class="fileEntry">
                                <td>File</td>
                                <td>Desc</td>
                                <td>RM Me</td>
                            </tr>
                        </tbody>
                    </table>
                </fieldset>
                <div class="buttons">
                    <%--jummp:button id="uploadButton" name="Upload">Upload</jummp:button --%>
                    <%--jummp:button id="cancelButton" name="Cancel">Cancel</jummp:button --%>
                    <g:submitButton name="Upload" value="Upload" />
                    <g:submitButton name="Cancel" value="Cancel" />
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
                    //todo: change the DOM elements that get created
                    $('<span>', {
                        class: 'fix'
                    }).append(
                        $("<input>", {
                            type: 'file',
                            class: "oneLiner"
                        }),
                        $('<input>', {
                            type: 'text',
                            value: 'Description',
                            class: "oneLiner"
                        }),
                        $('<a>', {
                            href: "javascript:alert('fail')",
                            class: 'oneLiner',
                            text: 'Remove file'
                        })
                    ).appendTo('#result')
                });
            }); 
$("#uploadButton").click( function() {
    $("#fileUpload").submit();
});
$("#cancelButton").click( function() {
    $("#fileUpload").reset();
});
        </g:javascript>
    </body>
</html>
