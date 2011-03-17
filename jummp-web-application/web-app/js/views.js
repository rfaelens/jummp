/**
 * View logic for /user/passwordForgotten/
 */
function loadPasswordForgottenCallback() {
    $("#password-forgotten-form div input").button();
    $("#password-forgotten-form div input:button").click(function() {
        submitForm($("#password-forgotten-form"), createLink("user", "requestPassword"), requestPasswordCallback);
    });
    $("#password-forgotten-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitForm($("#password-forgotten-form"), createLink("user", "requestPassword"), requestPasswordCallback);
        }
    });
    $("#password-forgotten-form").submit(function() { return false; });
}

/**
 * Callback for successful form submission to /user/requestPassword/
 * @param data JSON object returned by server
 */
function requestPasswordCallback(data) {
    if (data.error) {
        showErrorMessage(data.error);
    } else if (data.success) {
        $("#body").html("<p>" + i18n.user.resetPassword.passwordRequested + "</p>");
    }
}

/**
 * View logic for /model/
 */
function loadModelListCallback() {
    $("#navigationButtons a").button();
    $("#navigationButtons a").click(function() {
        loadView(createLink('model', 'upload'), loadUploadModelCallback);
    });
    $('#modelTable').dataTable({
        // TODO: in future it might be interesting to allow filtering
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        sAjaxSource: createLink('model', 'dataTableSource'),
        // TODO: move function into an own method,
        "fnServerData": function(sSource, aoData, fnCallback) {
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "error": function(jqXHR, textStatus, errorThrown) {
                    handleError($.parseJSON(jqXHR.responseText));
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
                    for (var i=0; i<json.aaData.length; i++) {
                        var rowData = json.aaData[i];
                        var id = rowData[0];
                        if (rowData[2] != null) {
                            var publication = rowData[2];
                            var html = "";
                            if (publication.linkProvider == "PUBMED") {
                                html = createPubMedLink(publication, id);
                            } else if (publication.linkProvider == "DOI") {
                                html = createDoiLink(publication, id);
                            } else if (publication.linkProvider == "URL") {
                                html = createPublicationLink(publication, id, publication.link);
                            }
                            rowData[2] = html;
                        }
                        // id column
                        rowData[0] = '<a href="#" onclick="showModel(\'' + id + '\');">' + id + '</a>';
                        // the format/download column
                        rowData[4] = rowData[4] + '&nbsp;<a href="' + createLink('model', 'download', id) + '">' + i18n.model.list.download + '</a>';
                    }
                    fnCallback(json);
                    $('a.tooltip').cluetip({width: 550, clickThrough: true, ajaxProcess: function(data) {
                        return $(data).not("h2");
                    }});
                }});
            },
        // i18n
        oLanguage: $.jummp.i18n.dataTables
    });
    $(document).bind("login", function(event) {
        $('#modelTable').dataTable().fnDraw();
    });
}

/**
 * View logic for /model/show/id/
 * @param data The original data retrieved through AJAX
 * @param tabIndex Optional selector for tab index to switch to after the tab view has been loaded
 */
function loadModelTabCallback(data, tabIndex) {
    $("#navigationButtons a").button();
    $("#navigationButtons a:eq(0)").click(function() {
        loadView(createLink('model', 'index'), loadModelListCallback);
    });
    $("#navigationButtons a:eq(1)").click(function() {
        loadView(createLink('model', 'upload'), loadUploadModelCallback);
    });
    $("#modelTabs").tabs({disabled: [1, 2, 3, 4, 5],
        ajaxOptions: {
            error: function(jqXHR) {
                $("#body").unblock();
                handleError($.parseJSON(jqXHR.responseText));
            },
            cache: false
        },
        load: function(event, ui) {
            // ui has index
            switch ($(ui.tab).attr("id")) {
            case "modelTabs-addRevision":
                // add revision tab
                $("#revision-upload-form div.ui-dialog-buttonpane input").button();
                $("#revision-upload-form div.ui-dialog-buttonpane input:button").click(function() {
                    submitFormWithFile($("#revision-upload-form"), createLink("model", "saveNewRevision"), uploadRevisionCallback);
                });
                break;
            }
        }
    });
    $("#modelTabs").show();
    if (tabIndex) {
        $("#modelTabs").tabs("select", $(tabIndex).attr("href"));
    }
}

/**
 * Callback for successful form submission to /model/saveNewRevision/
 * @param data JSON object returned by server
 */
function uploadRevisionCallback(data) {
    if (data.error) {
        showErrorMessage([data.model, data.comment]);
        setErrorState("#revision-upload-file", data.model);
        setErrorState("#revision-upload-comment", data.comment);
    } else if (data.success) {
        showInfoMessage(i18n.model.revision.upload.success.replace(/_NAME_/, data.revision.model.name), 20000);
        $("#modelTabs").tabs("select", 0);
    }
}

/**
 * View logic for /model/upload/
 */
function loadUploadModelCallback() {
    var uploadModelPublicationChangeListener = function() {
        var value = $("input:radio[name=publicationType]:checked").val();
        switch (value) {
        case "PUBMED":
            enableElement("#model-upload-pubmed", true);
            enableElement("#model-upload-doi", false);
            enableElement("#model-upload-url", false);
            $("#model-upload-publication-table").fadeOut("fast");
            break;
        case "DOI":
            enableElement("#model-upload-pubmed", false);
            enableElement("#model-upload-doi", true);
            enableElement("#model-upload-url", false);
            $("#model-upload-publication-table").fadeIn("fast");
            break;
        case "URL":
            enableElement("#model-upload-pubmed", false);
            enableElement("#model-upload-doi", false);
            enableElement("#model-upload-url", true);
            $("#model-upload-publication-table").fadeIn("fast");
            break;
        case "UNPUBLISHED":
            enableElement("#model-upload-pubmed", false);
            enableElement("#model-upload-doi", false);
            enableElement("#model-upload-url", false);
            $("#model-upload-publication-table").fadeOut("fast");
            break;
        }
    };
    $("#navigationButtons a").button();
    $("#navigationButtons a").click(function() {
        loadView(createLink('model', 'index'), loadModelListCallback);
    });
    $("input:radio[name=publicationType]")[0].checked = true;
    $("#model-upload-form div.ui-dialog-buttonpane input").button();
    $("#model-upload-form div.ui-dialog-buttonpane input:button").click(function() {
        submitFormWithFile($("#model-upload-form"), createLink("model", "save"), uploadModelCallback);
    });
    $("#model-upload-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitFormWithFile($("#model-upload-form"), createLink("model", "save"), uploadModelCallback);
        }
    });
    $("#model-upload-form").submit(function() { return false; });
    $("input:radio[name=publicationType]").change(uploadModelPublicationChangeListener);
    enableElement("#model-upload-publication-month", false);
    enableElement("#model-upload-publication-day", false);
    $("#model-upload-publication-year").change(function() {
        if ($("#model-upload-publication-year").val() == "") {
            $("#model-upload-publication-month").val("");
            $("#model-upload-publication-day").val("");
            enableElement("#model-upload-publication-month", false);
            enableElement("#model-upload-publication-day", false);
        } else {
            enableElement("#model-upload-publication-month", true);
        }
    });
    $("#model-upload-publication-month").change(function() {
        if ($("#model-upload-publication-month").val() == "") {
            $("#model-upload-publication-day").val("");
            enableElement("#model-upload-publication-day", false);
        } else {
            enableElement("#model-upload-publication-day", true);
        }
    });
    $("#model-upload-publication-author-add").button();
    $("#model-upload-publication-author-add").click(function() {
        // initials
        var initialsRow = $("#model-upload-publication-author-initials-row").clone();
        var counter = $("#model-upload-author-count").val();
        var initialsId = $("td label", initialsRow).attr("for") + counter;
        initialsRow.attr("id", initialsRow.attr("id") + counter);
        $("td label", initialsRow).attr("for", initialsId);
        $("td input", initialsRow).attr("id", initialsId);
        $("td input", initialsRow).attr("name", $("td input", initialsRow).attr("name") + counter);
        $("td input[type=text]", initialsRow).val("");
        // first name
        var firstNameRow = $("#model-upload-publication-author-firstname-row").clone();
        var firstNameId = $("td label", firstNameRow).attr("for") + counter;
        firstNameRow.attr("id", firstNameRow.attr("id") + counter);
        $("td label", firstNameRow).attr("for", firstNameId);
        $("td input", firstNameRow).attr("id", firstNameId);
        $("td input", firstNameRow).attr("name", $("td input", firstNameRow).attr("name") + counter);
        $("td input[type=text]", firstNameRow).val("");
        // last name
        var lastNameRow = $("#model-upload-publication-author-lastname-row").clone();
        var lastNameId = $("td label", lastNameRow).attr("for") + counter;
        lastNameRow.attr("id", lastNameRow.attr("id") + counter);
        $("td label", lastNameRow).attr("for", lastNameId);
        $("td input", lastNameRow).attr("id", lastNameId);
        $("td input", lastNameRow).attr("name", $("td input", lastNameRow).attr("name") + counter);
        $("td input[type=text]", lastNameRow).val("");
        // add to table
        $("#model-upload-publication-table tbody").append(initialsRow);
        $("#model-upload-publication-table tbody").append(firstNameRow);
        $("#model-upload-publication-table tbody").append(lastNameRow);
        setErrorState("#" + initialsId);
        setErrorState("#" + firstNameId);
        setErrorState("#" + lastNameId);
        // connect the remove button
        var removeButton = $("td input[type=button]", initialsRow).button();
        removeButton.click(function() {
            initialsRow.remove();
            firstNameRow.remove();
            lastNameRow.remove();
        });
        removeButton.show();
        $("#model-upload-author-count").val(parseInt(counter) + 1);
    });
    uploadModelPublicationChangeListener();
}

/**
 * Callback for successful form submission to /model/save/
 * @param data JSON object returned by server
 */
function uploadModelCallback(data) {
    if (data.error) {
        showErrorMessage([data.model, data.name, data.comment, data.pubmed, data.doi, data.url, data.publicationTitle, data.publicationJournal, data.publicationAffiliation, data.publicationAbstract, data.publicationYear, data.publicationMonth, data.publicationDay, data.authorInitials, data.authorFirstName, data.authorLastName]);
        setErrorState("#model-upload-file", data.model);
        setErrorState("#model-upload-name", data.name);
        setErrorState("#model-upload-comment", data.comment);
        setErrorState("#model-upload-pubmed", data.pubmed);
        setErrorState("#model-upload-doi", data.doi);
        setErrorState("#model-upload-url", data.url);
        setErrorState("#model-upload-publication-title", data.publicationTitle);
        setErrorState("#model-upload-publication-journal", data.publicationJournal);
        setErrorState("#model-upload-publication-affiliation", data.publicationAffiliation);
        setErrorState("#model-upload-publication-abstract", data.publicationAbstract);
        setErrorState("#model-upload-publication-year", data.publicationYear);
        setErrorState("#model-upload-publication-month", data.publicationMonth);
        setErrorState("#model-upload-publication-day", data.publicationDay);
        setErrorState("#model-upload-publication-author-initials", data.authorInitials);
        setErrorState("#model-upload-publication-author-firstname", data.authorFirstName);
        setErrorState("#model-upload-publication-author-lastname", data.authorLastName);
    } else if (data.success) {
        showInfoMessage(i18n.model.upload.success.replace(/_ID_/, data.model.id), 20000);
        showModel(data.model.id);
    }
}

/**
 * View logic for /themeing/themes/
 */
function loadThemeSelectionCallback() {
    $("#change-theme-form input:button").button();
    $("#change-theme-form input:button").click(function() {
        submitForm($("#change-theme-form"), createLink("themeing", "save"), changeThemeCallback);
    });
    $("#change-theme-form").submit(function() { return false; });
}

/**
 * Callback for successful form submission to /themeing/save/
 * @param data JSON object returned by server
 */
function changeThemeCallback(data) {
    if (data.error) {
        showErrorMessage(data.theme);
        setErrorState("#change-theme-themes", data.theme);
    } else if (data.success) {
        showInfoMessage(i18n.theme.success.replace(/_CODE_/, data.theme), 20000);
    }
}

/**
 * View logic for /user/
 */
function loadShowUserInfoCallback() {
    $("#body div.ui-dialog-buttonpane input").button();
    $("#edit-user-form div.ui-dialog-buttonpane input:button").click(function() {
        submitForm($("#edit-user-form"), createLink("user", "editUser"), editUserInfoCallback);
    });
    $("#edit-user-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitForm($("#edit-user-form"), createLink("user", "editUser"), editUserInfoCallback);
        }
    });
    $("#edit-user-form").submit(function() { return false; });
    $("#change-password-form div input:button").click(function() {
        submitForm($("#change-password-form"), createLink("user", "changePassword"), changePasswordCallback);
    });
    $("#change-password-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitForm($("#change-password-form"), createLink("user", "changePassword"), changePasswordCallback);
        }
    });
    $("#change-password-form").submit(function() { return false; });
}

/**
 * Callback for successful form submission to /user/editUser/
 * @param data JSON object returned by server
 */
function editUserInfoCallback(data) {
    if (data.error) {
        showErrorMessage([data.username, data.userRealName, data.email]);
        setErrorState("#edit-user-username", data.username);
        setErrorState("#edit-user-userrealname", data.userRealName);
        setErrorState("#edit-user-email", data.email);
    } else if (data.success) {
        showInfoMessage(i18n.user.editSuccess, 20000);
        setErrorState("#edit-user-username");
        setErrorState("#edit-user-userrealname");
        setErrorState("#edit-user-email");
    }
}

/**
 * Callback for successful form submission to /user/changePassword/
 * @param data JSON object returned by server
 */
function changePasswordCallback(data) {
    if (data.error) {
        if (data.error != true) {
            showErrorMessage(data.error);
        } else {
            showErrorMessage([data.oldPassword, data.newPassword, data.verifyPassword]);
        }
        setErrorState("#change-password-old", data.oldPassword);
        setErrorState("#change-password-new", data.newPassword);
        setErrorState("#change-password-verify", data.verifyPassword);
    } else if (data.success) {
        showInfoMessage(i18n.user.passwordChanged, 20000);
        setErrorState("#change-password-old");
        setErrorState("#change-password-new");
        setErrorState("#change-password-verify");
        $("#change-password-form input:password").val("");
    }
}

/**
 * View logic for /register/validate/
 */
function loadValidateRegistrationCallback() {
    $("#validate-registration-form div input").button();
    $("#validate-registration-form div input:button").click(function() {
        submitForm($("#validate-registration-form"), createLink("register", "validateRegistration"), validateRegistrationCallback);
    });
    $("#validate-registration-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitForm($("#validate-registration-form"), createLink("register", "validateRegistration"), validateRegistrationCallback);
        }
    });
    $("#validate-registration-form").submit(function() { return false; });
}

/**
 * Callback for successful form submission to /register/validateRegistration/
 * @param data JSON object returned by server
 */
function validateRegistrationCallback(data) {
    if (data.error) {
        showErrorMessage(data.error);
    } else if (data.success) {
        $("#body").html("<p>" + i18n.user.register.validate.success + "</p>");
    }
}

/**
 * View logic for /user/resetPassword/
 */
function loadResetPasswordCallback() {
    $("#reset-password-form div input").button();
    $("#reset-password-form div input:button").click(function() {
        submitForm($("#reset-password-form"), createLink('user', 'performResetPassword'), resetPasswordCallback);
    });
    $("#reset-password-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            submitForm($("#reset-password-form"), createLink('user', 'performResetPassword'), resetPasswordCallback);
        }
    });
    $("#reset-password-form").submit(function() { return false; });
}

/**
 * Callback after sending reset-password-form to /user/performResetPassword/
 * @param data JSON object returned by server
 */
function resetPasswordCallback(data) {
    if (data.error) {
        if (data.error != true) {
            showErrorMessage(data.error);
        }
        showErrorMessage([data.username, data.password, data.verifyPassword, data.code]);
        setErrorState("#reset-password-form-username", data.username);
        setErrorState("#reset-password-form-password", data.password);
        setErrorState("#reset-password-form-verifyPassword", data.verifyPassword);
    } else if (data.success) {
        showInfoMessage(i18n.user.resetPassword.success, 20000);
    }
}

/**
 * View logic for /userAdministration/
 */
function loadUserListCallback() {
    var createUserChangeMarkup = function(id, target, enabled) {
        var checkboxId = "user-change-" + id + "-" + target;
        var html = '<input type="checkbox" id="' + checkboxId + '" ';
        if (enabled) {
            html += 'checked="checked"';
        }
        html += '/><input type="button" value="' + i18n.ui.button.update + '" onclick="changeUser(' + id + ', \'' + checkboxId + '\', \'' + target + '\')"/>';
        return html;
    };
    // TODO: merge the datatables as far as possible
    $('#userTable').dataTable({
        // TODO: in future it might be interesting to allow filtering
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        sAjaxSource: createLink('userAdministration', 'dataTableSource'),
        // TODO: move function into an own method,
        "fnServerData": function(sSource, aoData, fnCallback) {
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "error": function(jqXHR, textStatus, errorThrown) {
                    handleError($.parseJSON(jqXHR.responseText));
                    // clear the table
                    fnCallback({aaData: [], iTotalRecords: 0, iTotalDisplayRecords: 0});
                },
                "success": function(json) {
                    for (var i=0; i<json.aaData.length; i++) {
                        var rowData = json.aaData[i];
                        var id = rowData[0];
                        rowData[0] = "<a href=\"#\">" + id + "</a>";
                        rowData[4] = createUserChangeMarkup(id, 'enable', rowData[4]);
                        rowData[5] = createUserChangeMarkup(id, 'expireAccount', rowData[5]);
                        rowData[6] = createUserChangeMarkup(id, 'lockAccount', rowData[6]);
                        rowData[7] = createUserChangeMarkup(id, 'expirePassword', rowData[7]);
                    }
                    fnCallback(json);
                    $("#userTable tr input:button").button();
                    $("#userTable tr td a").click(function() {
                        loadView(createLink('userAdministration', 'show', $(this).text()), loadAdminUserCallback);
                    })
                }
            });
        },
        // i18n
        oLanguage: $.jummp.i18n.dataTables
    });
}

/**
 * View logic for /userAdministration/show
 */
function loadAdminUserCallback() {
    var positionAreas = function () {
        // TODO: this seems not the best solution - the view receives a horizontal scrollbar
        $("#availableRoles").position({
            my: "top",
            at: "left top",
            of: "#userRoles",
            collision: "flip flip"
        });
    };
    $("#body div.ui-dialog-buttonpane input").button();
    $("#user-role-management table tr a").button();
    $("#user-role-management table tr a").click(function() {
        $("#body").block();
        var link = $(this);
        var id = link.prev().val();
        var container = link.parents("div")[0];
        var userId = $($("input", container)[0]).val();
        var action = $($("input", container)[1]).val();
        $.ajax({
            type: 'GET',
            url: createLink("userAdministration", action, id) + "?userId=" + userId,
            dataType: 'json',
            cache: 'false',
            success: function (data) {
                if (handleError(data)) {
                    // TODO: with jquery 1.5 should be handled by status code function
                    return;
                }
                clearErrorMessages();
                if (data.error) {
                    showErrorMessage(data.error);
                } else if (data.success) {
                    showInfoMessage(i18n.userAdministration.success, 20000);
                    var linkText = "";
                    var divInsertId = "";
                    if (action == "addRole") {
                        linkText = i18n.userAdministration.ui.removeRole;
                        divInsertId = "#userRoles";
                    } else if (action == "removeRole") {
                        linkText = i18n.userAdministration.ui.addRole;
                        divInsertId = "#availableRoles";
                    }
                    $("span", link).text(linkText);
                    var tableRow = link.parents("tr");
                    tableRow.detach();
                    tableRow.appendTo($("table tbody", $(divInsertId)));
                    positionAreas();
                }
                $("#body").unblock();
            },
            error: function(jqXHR) {
                $("#body").unblock();
                handleError($.parseJSON(jqXHR.responseText));
            }
        });
        createLink("userAdministration", "addRole", id)
    });
    $("#edit-user-form div.ui-dialog-buttonpane input:button").click(function() {
        // yes it is intended to submit to the user controller - the action works for admin users, too
        submitForm($("#edit-user-form"), createLink("user", "editUser"), editUserInfoCallback);
    });
    positionAreas();
}

/**
 * View logic for /userAdministration/register
 */
function loadAdminRegisterCallback() {
    $("#body form div input").button();
    $("#body form div input:button").click(function() {
        submitForm($("#registerForm"), createLink("userAdministration", "performRegistration"), adminRegisterCallback);
    })
}

/**
 * Callback for successful form submission to /userAdministration/performRegistration.
 * In success case the edit user view is loaded.
 * @param data JSON object returned by server
 */
function adminRegisterCallback(data) {
    if (data.error) {
        showErrorMessage([data.username, data.email, data.userRealName]);
        setErrorState("#register-form-username", data.username);
        setErrorState("#register-form-email", data.email);
        setErrorState("#register-form-name", data.userRealName);
    } else if (data.success) {
        showInfoMessage(i18n.userAdministration.register.success.replace(/_CODE_/, data.user), 20000);
        loadView(createLink("userAdministration", "show", data.user), loadAdminUserCallback);
    }
}
