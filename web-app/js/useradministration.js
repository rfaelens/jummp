$.jummp.userAdministration = new Object();
$.jummp.userAdministration.changeUser = function(userId, field, target) {
    $.ajax({
        url: target + "/" + userId,
        dataType: 'json',
        data: {value: $("#" + field).attr("checked") == "checked" ? true : false},
        cache: 'false',
        success: function(data) {
            // redraw the dataTable to reset all changes
            $('#userTable').dataTable().fnDraw();
        }
    })
};

$.jummp.userAdministration.loadUserList = function() {
    var createUserChangeMarkup = function(id, target, enabled) {
        var checkboxId = "user-change-" + id + "-" + target;
        var html = '<input type="checkbox" id="' + checkboxId + '" ';
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
                "error": function(jqXHR, textStatus, errorThrown) {
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
                    for (var i=0; i<json.aaData.length; i++) {
                        var rowData = json.aaData[i];
                        var id = rowData[0];
                        rowData[0] = "<a href=\"show/" + id + "\">" + id + "</a>"
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
    $("#user-role-management table tr a").click(function() {
        var link = $(this);
        var id = link.prev().val();
        var container = link.parents("div")[0];
        var userId = $($("input", container)[0]).val();
        var action = $($("input", container)[1]).val();
        $.ajax({
            type: 'GET',
            url: "../" + action + "/" + id + "?userId=" + userId,
            dataType: 'json',
            cache: 'false',
            success: function (data) {
                if (data.error) {
                    alert(data.error);
                } else if (data.success) {
                    var linkText = "";
                    var divInsertId = "";
                    if (action == "addRole") {
                        linkText = "Remove Role from User";
                        divInsertId = "#userRoles";
                    } else if (action == "removeRole") {
                        linkText = "Add Role to User";
                        divInsertId = "#availableRoles";
                    }
                    var tableRow = link.parents("tr");
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
                // TODO: proper error and success notifications
                if (data.error) {
                    alert((data.username ? data.username : '') + (data.userRealName ? data.userRealName : '') + (data.email ? data.email : ''));
                } else if (data.success) {
                    alert("User updated successfully");
                }
            }
        });
    });
};

$.jummp.userAdministration.register = function() {
    $("#registerForm").submit(function(event) {
        event.preventDefault();
        $.ajax({
            type: 'GET',
            url: "userAdministration/performRegistration",
            dataType: 'json',
            cache: 'false',
            data: {
                username: $("#register-form-username").val(),
                userRealName: $("#register-form-name").val(),
                email: $("#register-form-email").val()
            },
            success: function (data) {
                // TODO: proper error and success notifications
                if (data.error) {
                    alert((data.username ? data.username : '') + (data.userRealName ? data.userRealName : '') + (data.email ? data.email : ''));
                } else if (data.success) {
                    alert("User successfully registered");
                }
            }
        });
    });
};
