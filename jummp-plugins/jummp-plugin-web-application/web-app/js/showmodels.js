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
                $("#overlayNav div").click(function() {
                    $.jummp.showModels.loadView($(this));
                });
                $.jummp.showModels.loadView($("#overlayNav div").first());
            });
        },
        onLoad: function() {
            if ($("#sidebar-element-last-accessed-models").get(0)) {
                $.jummp.showModels.lastAccessedModels($("#sidebar-element-last-accessed-models"));
            }
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
 * @param element The jQuery element which got clicked
 */
$.jummp.showModels.loadView = function(element) {
    $("#overlayContentContainer").block();
    $.ajax({
        url: element.attr("rel"),
        dataType: 'HTML',
        type: 'GET',
        cache: 'false',
        success: function(data) {
            $("#overlayContentContainer").unblock();
            $("#overlayContentContainer").html(data);
            $("#overlayNav div").removeClass("selected");
            element.addClass("selected");
        },
        error: function(jqXHR) {
            $("#overlayContentContainer").unblock();
        }
    });
};

$.jummp.showModels.lastAccessedModels = function(container) {
    $.ajax({
        url: $.jummp.createLink("search", "lastAccessedModels"),
        dataType: 'JSON',
        cache: false, // makes IE happy
        success: function(data) {
            if (data.length == 0) {
                $("h2", container).text($.i18n.prop("model.history.empty"));
                $("p", container).text("");
                return;
            }
            $("h2", container).text($.i18n.prop("model.history.explanation"));
            $("p", container).text("");
            var ul = $("<ul/>");
            for (var i=0; i<data.length; i++) {
                ul.append("<li>" + data[i].name.replace(/_/g, " ") + "<br/>"
                + $.i18n.prop("model.history.submitter") + " " + data[i].submitter +
                "<br/><a href=\"#\" rel=\"" + data[i].id + "\">" + $.i18n.prop("model.history.link") + "</a></li>");
            }
            $("p", container).append(ul);
            $("p ul li a", container).click(function() {
                $.jummp.showModels.showOverlay($.jummp.createLink("search", "model", $(this).prop("rel")));
            });
        }
    });
};
