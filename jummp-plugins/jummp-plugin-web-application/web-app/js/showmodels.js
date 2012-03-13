$.jummp.showModels = new Object();
$.jummp.showModels.changeModel = function(userId, field, target) {
    $.ajax({
        url: target + "/" + userId,
        dataType: 'json',
        data: {value: $("#" + field).attr("checked") == "checked" ? true : false},
        cache: 'false',
        success: function(data) {
            // redraw the dataTable to reset all changes
            $('#modelTable').dataTable().fnDraw();
        }
    })
};

$.jummp.showModels.loadModelList = function() {
    var createModelChangeMarkup;
    $('#modelTable').dataTable({
        // TODO: in future it might be interesting to allow filtering
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        sAjaxSource: 'dataTableSource',
        "fnServerData": function(sSource, aoData, fnCallback) {
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "error": function(jqXHR, textStatus, errorThrown) {
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
                    for (var i=0; i<json.aaData.length; i++) {
                        var rowData = json.aaData[i];
                        var id = rowData[0];
                        rowData[0] = "<a class='animate' onclick=\"$.jummp.showModels.showOverlay('" + $.jummp.createLink("search", "model", id) + "');\" href=\"#\">" + id + "</a>";
                        rowData[1] = rowData[1] ? rowData[1].replace(/_/g, " ") : "-";
                        rowData[2] = rowData[2] ? rowData[2].title : "-";
                    }
                    fnCallback(json);
                }
            });
        }
    });
};

$.jummp.showModels.showOverlay = function(overlayLink) {
    $("#overlayContainer").data("linkTarget", overlayLink);
    if ($("#overlayContainer").data("overlay")) {
        $("#overlayContainer").data("overlay").load();
        return;
    }
    $("#overlayContainer").overlay({
        onBeforeLoad: function() {
            // grab wrapper element inside content
            var wrap = this.getOverlay().find(".contentWrap");
            wrap.load($("#overlayContainer").data("linkTarget"), function() {
                $("#overlayContainer button.close").click(function() {
                    $("#overlayContainer").data("overlay").close();
                });
            });
        },
        // some mask tweaks suitable for modal dialogs
        mask: {
            color: 'black',
            loadSpeed: 200,
            opacity: 0.8
        },
        closeOnClick: false,
        load: true
    });
};

/**
 * Loads a new view for the #overlayContentContainer element through AJAX.
 * @param controller The controller to be called
 * @param action The action to be executed within the controller
 * @param id The ID of the model of which content is to be loaded
 * @param loadCallback A callback to execute after successfully updating the view.
 */
$.jummp.showModels.loadView = function(controller, method, id, loadCallback) {
    var url = $.jummp.createLink(controller, method, id);
    $("#overlayContentContainer").block();
    $.ajax({
        url: url,
        dataType: 'HTML',
        type: 'GET',
        cache: 'false',
        success: function(data) {
            $("#overlayContentContainer").unblock();
            $.jummp.clearErrorMessages();
            $("#overlayContentContainer").html(data);
            loadCallback(data, callbackData);
        },
        error: function(jqXHR) {
            $("#overlayContentContainer").unblock();
        }
    });
};

$.jummp.showModels.loadModelTabs = function (data, tabIndex) {
    $("#modelTabs").tabs({disabled: [6],
        ajaxOptions: {
            error: function(jqXHR) {
                $("#overlayContentContainer").unblock();
                handleError($.parseJSON(jqXHR.responseText));
            },
            cache: false
        },
        load: function(event, ui) {
            // ui has index
            switch ($(ui.tab).attr("id")) {
            case "modelTabs-overview":
                // add tooltips to the rows
                $("#model-math tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                $("#model-parameters tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                $("#model-entity tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                break;
            case "modelTabs-math":
                // add tooltips to the rows
                $("#model-math tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                break;
            case "modelTabs-parameter":
                // add tooltips to the rows
                $("#model-parameters tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                break;
            case "modelTabs-entity":
                // add tooltips to the rows
                $("#model-entity tbody tr").cluetip({clickThrough: false, sticky: true, mouseOutClose: true});
                break;
            case "modelTabs-revisions":
                $("#model-revisions table tr td.revisionNumber a").button();
                $("#model-revisions table tr td.revisionNumber a").click(function() {
                    changeModelTabRevision($(this).text());
                    $("#modelTabs").tabs("select", $("#modelTabs-model").attr("href"));
                });
                $("#model-revisions table tr td.revisionControl a").button();
                $("#model-revisions table tr td.revisionControl a.delete").button("option", "icons", {primary:'ui-icon-trash'});
                $("#model-revisions table tr td.revisionControl a.delete").click(function() {
                    if (confirm(i18n.model.revision.deleteRevision.verify)) {
                        submitForm($("#model-revisions table tr td.revisionControl form[name=delete]"), $.jummp.createLink("model", "deleteRevision"), function(data) {
                            if (data.deleted) {
                                showInfoMessage(i18n.model.revision.deleteRevision.success, 20000);
                                if ($("#model-revisions table tr").size() == 1) {
                                    // User has no longer access to the model, may be deleted
                                    $.jummp.showModels.loadView($.jummp.createLink('model', 'index'), loadModelListCallback);
                                } else {
                                    // there is at least one more revision
                                    var newLatestRevision = $("#model-revisions table tr td.revisionNumber:eq(1) a").text();
                                    changeModelTabRevision(newLatestRevision);
                                    $("#modelTabs").tabs("load", $("#modelTabs-revisions").attr("href"));
                                }
                            } else {
                                showInfoMessage(i18n.model.revision.deleteRevision.error, 20000);
                            }
                        });
                    }
                });
                // show diff
                $("#model-revisions table tr td a.diff").button();
                $("#model-revisions table tr td a.diff").click(function() {
                    var recentRevision = $("#model-revisions table tr td input:radio[name='recentRevision']:checked").val();
                    var previousRevision = $("#model-revisions table tr td input:radio[name='previousRevision']:checked").val();
                    if (recentRevision == previousRevision) {
                        // TODO remove string
                        showInfoMessage("Unable to show diff between the revision and itself. Please choose two different revisions.", 20000);
                        return false;
                    }
                    $.jummp.showModels.loadView($(this).attr("href") + "/?prevRev=" + previousRevision + "&currRev=" + recentRevision, showDiffDataCallback);
                    return false;
                });
                // add revision
                $("#revision-upload-form div.ui-dialog-buttonpane input").button();
                $("#revision-upload-form div.ui-dialog-buttonpane input:button").click(function() {
                    submitFormWithFile($("#revision-upload-form"), $.jummp.createLink("model", "saveNewRevision"), uploadRevisionCallback);
                });
                break;
            case "modelTabs-addRevision":
                break;
            }
        }
    });
    $("#modelTabs").show();
    if (tabIndex) {
        $("#modelTabs").tabs("select", $(tabIndex).attr("href"));
    }
};
