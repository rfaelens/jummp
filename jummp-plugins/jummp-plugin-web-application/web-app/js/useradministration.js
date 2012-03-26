/*global $: false
 */
$.jummp.userAdministration = {};
$.jummp.userAdministration.changeUser = function(userId, field, target) {
    "use strict";
    $.ajax({
        url: target + "/" + userId,
        dataType: 'json',
        data: {value: $("#" + field).attr("checked") === "checked" ? true : false},
        cache: 'false',
        success: function() {
            // redraw the dataTable to reset all changes
            $('#userTable').dataTable().fnDraw();
        }
    });
};

$.jummp.userAdministration.loadUserList = function() {
    "use strict";
    var createUserChangeMarkup = function(id, target, enabled) {
        var html, checkboxId;
        checkboxId = "user-change-" + id + "-" + target;
        html = '<input type="checkbox" id="' + checkboxId + '" ';
        if (enabled) {
            html += 'checked="checked"';
        }
        html += '/><input type="button" value="update" onclick="$.jummp.userAdministration.changeUser(' + id + ', \'' + checkboxId + '\', \'' + target + '\')"/>';
        return html;
    };
    $('#userTable').dataTable({
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
                "error": function() {
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
                    var rowData, id, i;
                    for (i=0; i<json.aaData.length; i++) {
                        rowData = json.aaData[i];
                        id = rowData[0];
                        rowData[0] = "<a href=\"show/" + id + "\">" + id + "</a>";
                        rowData[4] = createUserChangeMarkup(id, 'enable', rowData[4]);
                        rowData[5] = createUserChangeMarkup(id, 'expireAccount', rowData[5]);
                        rowData[6] = createUserChangeMarkup(id, 'lockAccount', rowData[6]);
                        rowData[7] = createUserChangeMarkup(id, 'expirePassword', rowData[7]);
                    }
                    fnCallback(json);
                }
            });
        }
    });
};

$.jummp.userAdministration.editUser = function() {
    "use strict";
    $("#user-role-management table tr a").click(function() {
        var link, id, container, userId, action;
        link = $(this);
        id = link.prev().val();
        container = link.parents("div")[0];
        userId = $($("input", container)[0]).val();
        action = $($("input", container)[1]).val();
        $.ajax({
            type: 'GET',
            url: "../" + action + "/" + id + "?userId=" + userId,
            dataType: 'json',
            cache: 'false',
            success: function (data) {
                if (data.error) {
                    $.jummp.errorMessage(data.error);
                } else if (data.success) {
                    var linkText, divInsertId, tableRow;
                    linkText = "";
                    divInsertId = "";
                    if (action === "addRole") {
                        linkText = $.i18n.prop("user.administration.userRole.ui.removeRole");
                        divInsertId = "#userRoles";
                    } else if (action === "removeRole") {
                        linkText = $.i18n.prop("user.administration.userRole.ui.addRole");
                        divInsertId = "#availableRoles";
                    }
                    tableRow = link.parents("tr");
                    $("a", tableRow).text(linkText);
                    tableRow.detach();
                    tableRow.appendTo($("table tbody", $(divInsertId)));
                }
            }
        });
    });
    $("#edit-user-form").submit(function(event) {
        event.preventDefault();
        $.ajax({
            type: 'GET',
            url: "../editUser",
            dataType: 'json',
            cache: 'false',
            data: {
                username: $("#edit-user-username").val(),
                userRealName: $("#edit-user-userrealname").val(),
                email: $("#edit-user-email").val()
            },
            success: function (data) {
                if (data.error) {
                    $.jummp.warningMessage(data.username);
                    $.jummp.warningMessage(data.userRealName);
                    $.jummp.warningMessage(data.email);
                } else if (data.success) {
                    $.jummp.infoMessage($.i18n.prop("user.administration.edit.success"));
                }
            }
        });
    });
};

$.jummp.userAdministration.register = function() {
    "use strict";
    $("#registerForm").submit(function(event) {
        event.preventDefault();
        $.ajax({
            type: 'GET',
            url: "performRegistration",
            dataType: 'json',
            cache: 'false',
            data: {
                username: $("#register-form-username").val(),
                userRealName: $("#register-form-name").val(),
                email: $("#register-form-email").val()
            },
            success: function (data) {
                if (data.error) {
                    $.jummp.warningMessage(data.username);
                    $.jummp.warningMessage(data.userRealName);
                    $.jummp.warningMessage(data.email);
                } else if (data.success) {
                    $.jummp.infoMessage($.i18n.prop("user.administration.register.success", data.user));
                }
            }
        });
    });
};
