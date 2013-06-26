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
                    Please do not submit your model multiple times.
                </g:if>
            </div>
        </g:hasErrors>
        <h1>Upload Files</h1>
        <p style="padding-bottom:1em">Can I has files plz?</p>
        <g:uploadForm id="fileUpload" novalidate="false" autocomplete="false">
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
                    <table class='formtable' id="additionalFiles">
                        <tbody>
                        </tbody>
                    </table>
                </fieldset>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="Abort" />
                    <g:submitButton name="Back" value="Back" />
                    <g:submitButton name="Upload" value="Upload" />
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
                                type: 'file'
                            })
                        ),
                        $('<td>').append(
                            $('<input>', {
                                type: 'text',
                                class: 'description',
                                value: 'Description'
                            })
                        ),
                        $('<td>').append(
                            $('<a>', {
                                href: "javascript:alert('fail')",
                                class: 'killer normalAnchor',
                                text: 'Discard'
                            })
                        )
                    ).appendTo('table#additionalFiles')
                });
            });

            $("#uploadButton").click( function() {
                $("#fileUpload").submit();
            });

            $("#cancelButton").click( function() {
                $("#fileUpload").reset();
            });

            $(document).on("focus", ".description", function(){
                if ($(this).data("reset") === undefined) {
                    $(this).val("");
                    $(this).data("reset", true);
                }
            });
        </g:javascript>
    </body>
</html>
