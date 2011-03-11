/**
 * View logic for /user/passwordForgotten/
 */
function loadPasswordForgottenCallback() {
    $("#password-forgotten-form div input").button();
    $("#password-forgotten-form div input:button").click(requestPassword);
    $("#password-forgotten-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            requestPassword();
        }
    });
}

/**
 * View logic for /model/
 */
function loadModelListCallback() {
    $("#navigationButtons a").button();
    $("#navigationButtons a").click(function() {
        loadView(createLink('model', 'upload'), loadUploadModelCallback);
    });
    createModelDataTable();
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
        ajaxOptions: {error: function(jqXHR) {
            $("#body").unblock();
            handleError($.parseJSON(jqXHR.responseText));
        }},
        load: function(event, ui) {
            // ui has index
            switch ($(ui.tab).attr("id")) {
            case "modelTabs-addRevision":
                // add revision tab
                $("#revision-upload-form div.ui-dialog-buttonpane input").button();
                $("#revision-upload-form div.ui-dialog-buttonpane input:button").click(uploadRevision);
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
    $("#model-upload-form div.ui-dialog-buttonpane input:button").click(uploadModel);
    $("#model-upload-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            uploadModel();
        }
    });
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
 * View logic for /themeing/themes/
 */
function loadThemeSelectionCallback() {
    $("#change-theme-form input:button").button();
    $("#change-theme-form input:button").click(changeTheme);
}

/**
 * View logic for /user/
 */
function loadShowUserInfoCallback() {
    $("#body div.ui-dialog-buttonpane input").button();
    $("#edit-user-form div.ui-dialog-buttonpane input:button").click(editUser);
    $("#edit-user-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            editUser();
        }
    });
    $("#change-password-form div input:button").click(changePassword);
    $("#change-password-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            changePassword();
        }
    });
}

/**
 * View logic for /register/validate/
 */
function loadValidateRegistrationCallback() {
    $("#validate-registration-form div input").button();
    $("#validate-registration-form div input:button").click(validateRegistration);
    $("#validate-registration-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            validateRegistration();
        }
    });
}

/**
 * View logic for /user/resetPassword/
 */
function loadResetPasswordCallback() {
    $("#reset-password-form div input").button();
    $("#reset-password-form div input:button").click(resetPassword);
    $("#reset-password-form table input").keyup(function(event) {
        if (event.keyCode == 13) {
            resetPassword();
        }
    });
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
                        rowData[4] = createUserChangeMarkup(id, 'enable', rowData[4]);
                        rowData[5] = createUserChangeMarkup(id, 'expireAccount', rowData[5]);
                        rowData[6] = createUserChangeMarkup(id, 'lockAccount', rowData[6]);
                        rowData[7] = createUserChangeMarkup(id, 'expirePassword', rowData[7]);
                    }
                    fnCallback(json);
                    $("#userTable tr input:button").button();
                }
            });
        },
        // i18n
        oLanguage: {
            oPaginate: {
                sFirst:    i18n.dataTables.paginate.first,
                sLast:     i18n.dataTables.paginate.last,
                sNext:     i18n.dataTables.paginate.next,
                sPrevious: i18n.dataTables.paginate.previous
            },
            sEmptyTable:   i18n.dataTables.empty,
            sInfo:         i18n.dataTables.info,
            sInfoEmpty:    i18n.dataTables.infoEmpty,
            sInfoFiltered: i18n.dataTables.infoFiltered,
            sLengthMenu:   i18n.dataTables.lengthMenu,
            sProcessing:   i18n.dataTables.processing,
            sSearch:       i18n.dataTables.search,
            sZeroRecords:  i18n.dataTables.noFilterResults
        }
    });
}