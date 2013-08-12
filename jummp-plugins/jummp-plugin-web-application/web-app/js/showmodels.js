/*global $: false
 */
$.jummp.showModels = {};
$.jummp.showModels.changeModel = function (userId, field, target) {
    "use strict";
    $.ajax({
        url: target + "/" + userId,
        dataType: 'json',
        data: {value: $("#" + field).attr("checked") === "checked" ? true : false},
        cache: 'false',
        success: function () {
            // redraw the dataTable to reset all changes
            $('#modelTable').dataTable().fnDraw();
        }
    });
};

$.jummp.showModels.loadModelList = function () {
    "use strict";
    $('#modelTable').dataTable({
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bSort: true,
        bJQueryUI: true,
        bAutoWidth: false,
        sAjaxSource: 'dataTableSource',
        aoColumnDefs: [
            { bSortable: false, aTargets: [2] }
        ],
        sPaginationType: "full_numbers",
        iDisplayLength: 10,
        bLengthChange: false,
        bScrollInfinite: false,
        bScrollCollapse: true,
        sScrollY: "500px",
        bDeferRender: true,
        "fnServerData": function (sSource, aoData, fnCallback) {
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "error": function () {
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function (json) {
                    var i, rowData, id;
                    for (i = 0; i < json.aaData.length; i += 1) {
                        rowData = json.aaData[i];
                        id = rowData[0];
                        rowData[0] = "<a class='animate' onclick=\"$.jummp.showModels.showOverlay('" + $.jummp.createLink("model", "model", id) + "');\" href=\"#\">" + id + "</a>";
                        rowData[1] = rowData[1] ? rowData[1].replace(/_/g, " ") : "-";
                        rowData[2] = rowData[2] ? rowData[2].title : "-";
                    }
                    fnCallback(json);
                }
            });
        }
    });
};

$.jummp.showModels.showOverlay = function (overlayLink, closeCallback) {
    "use strict";
    $("#overlayContainer").data("linkTarget", overlayLink);
    if ($("#overlayContainer").data("overlay")) {
        $("#overlayContainer").data("overlay").load();
        return;
    }
    $("#overlayContainer").overlay({
        onBeforeLoad: function () {
            // grab wrapper element inside content
            var wrap = this.getOverlay().find(".contentWrap");
            wrap.load($("#overlayContainer").data("linkTarget"), function () {
                $("#overlayContainer button.close").click(function () {
                    $("#overlayContainer").data("overlay").close();
                });
                //$('overlayHeadline h1').cluetip({showTitle: false});
                $("#modelNav").hide();
                $("#overlayNav div").click(function () {
                    if ($(this).hasClass("overview")) {
                        $("#modelNav").show();
                    } else {
                        $("#modelNav").hide();
                    }
                    $.jummp.showModels.loadView($(this));
                });
                $("#modelNav div").click(function() {
                    $.jummp.showModels.loadModelNav($(this));
                });
                $.jummp.showModels.loadView(
                    $("#overlayNav div").first());
                });
        },
        onLoad: function () {
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
        load: true,
        fixed: false,
        onClose: closeCallback
    });
};

/**
 * Loads a new view for the #overlayContentContainer element through AJAX.
 * @param element The jQuery element which got clicked
 */
$.jummp.showModels.loadView = function (element) {
    "use strict";
    $("#overlayContentContainer").block();
    $.ajax({
        url: element.attr("rel"),
        dataType: 'HTML',
        type: 'GET',
        cache: 'false',
        success: function (data) {
            $("#overlayContentContainer").unblock();
            $("#overlayContentContainer").html(data);
            $("#overlayNav div").removeClass("selected");
            element.addClass("selected");
        },
        error: function () {
            $("#overlayContentContainer").unblock();
        }
    });
};

/**
 * Loads a new view of the model navigation for the #overlayContentContainer
 * element through AJAX.
 * @param element The jQuery element which got clicked
 */
$.jummp.showModels.loadModelNav = function (element) {
    "use strict";
    $("#overlayContentContainer").block();
    $.ajax({
        url: element.attr("rel"),
        dataType: 'HTML',
        type: 'GET',
        cache: 'false',
        success: function (data) {
            $("#overlayContentContainer").unblock();
            $("#overlayContentContainer").html(data);
            $("#modelNav div").removeClass("selected");
            element.addClass("selected");
        },
        error: function () {
            $("#overlayContentContainer").unblock();
        }
    });
};

$.jummp.showModels.lastAccessedModels = function (container) {
    "use strict";
    $.ajax({
        url: $.jummp.createLink("search", "lastAccessedModels"),
        dataType: 'JSON',
        cache: false, // makes IE happy
        success: function (data) {
            if (data.length === 0) {
                $("h3", container).text($.i18n.prop("model.history.empty"));
                $("p", container).text("");
                return;
            }
            var ul, i;
            $("h3", container).text($.i18n.prop("model.history.explanation"));
            $("p", container).text("");
            ul = $("<ul/>");
            for (i = 0; i < data.length; i += 1) {
                ul.append("<li>" + data[i].name.replace(/_/g, " ") + "<br/>" + $.i18n.prop("model.history.submitter") + " " + data[i].submitter + "<br/><a href=\"#\" rel=\"" + data[i].id + "\">" + $.i18n.prop("model.history.link") + "</a></li>");
            }
            $("p", container).append(ul);
            $("p ul li a", container).click(function () {
                $.jummp.showModels.showOverlay($.jummp.createLink("model", "model", $(this).prop("rel")));
            });
        }
    });
};
