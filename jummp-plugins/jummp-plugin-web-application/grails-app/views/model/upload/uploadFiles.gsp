<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Upload Files</title>
    </head>
    <body>
        <h1>Upload Files</h1>
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
                <div class="buttons">
                    <jummp:button id="uploadButton" name="Upload">Upload</jummp:button>
                    <jummp:button id="cancelButton" name="Cancel">Cancel</jummp:button>
                </div>
            </div>
        </g:uploadForm>
        <g:javascript>
$("#uploadButton").click( function() {
    $("#fileUpload").submit();
});
$("#cancelButton").click( function() {
    $("#fileUpload").reset();
});
        </g:javascript>
    </body>
</html>
