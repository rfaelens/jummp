<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>











<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RepositoryFileTransportCommand" %>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.upload.header"/></title>
        <link rel="stylesheet" href="<g:resource dir="css/jqueryui/smoothness" file="jquery-ui-1.10.3.custom.css"/>" />
        <g:javascript src="jquery/jquery-ui-v1.10.3.js"/>
        <style>
            .normalAnchor {
                color: #CCCC00;
            }
        </style>
        <g:if test ="${showProceedWithoutValidationDialog}">
          <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
          <script>
              $(function() {
                  $( "#dialog-confirm" ).dialog({
                        resizable: false,
                        height:300,
                        modal: true,
                        buttons: {
                            "Proceed Without Validation": function() {
                                document.getElementById('_eventId_ProceedWithoutValidation').click();
                                $( this ).dialog( "close" );
                            },
                            Cancel: function() {
                                $( this ).dialog( "close" );
                       }
                    }
                });
            });
        </script>
      </g:if>
    </head>
    <body>
        <g:if test ="${showProceedWithoutValidationDialog}">
          <div id="dialog-confirm" title="Validation Error">
            <p>The model files did not pass validation. Would you like to proceed?</p>
          </div>
        </g:if>
        <g:render template="/templates/errorMessage"/>
        <h2><g:message code="submission.upload.header"/></h2>
        <p style="padding-bottom:1em"><g:message code="submission.upload.explanation"/></p>
        <g:uploadForm id="fileUpload" novalidate="false" autocomplete="false">
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <g:if test="${workingMemory.containsKey("repository_files")}">
                          <g:each in="${workingMemory.get("repository_files")}">
                                <tr class="prop">
                                    <td class="name">
                                      <p>${(new File((it as RepositoryFileTransportCommand).path)).getName()}</p>
                                    </td>
                                    <td class="classification">
                                      <p>${(it as RepositoryFileTransportCommand).mainFile ? "(main file)" : "(additional file)"}</p>
                                    </td>
                                </tr>
                          </g:each>
                        </g:if>
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
                <fieldset>
                    <legend>
                        <g:message code="submission.upload.additionalFiles.legend"/>
                    </legend>
                    <a href="#" id="addFile" class="normalAnchor"><g:message code="submission.upload.additionalFiles.addButton" /></a>
                    <table class='formtable' id="additionalFiles">
                        <tbody>
                        </tbody>
                    </table>
                </fieldset>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:submitButton name="Upload" value="${g.message(code: 'submission.upload.uploadButton')}" />
                    <g:if test ="${showProceedWithoutValidationDialog}">
                      <g:submitButton name="ProceedWithoutValidation" value="ProceedWithoutValidation" hidden="true"/> 
                    </g:if>
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
   <g:render template="/templates/decorateSubmission" />
