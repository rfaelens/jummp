/**
 * Updates the user information panel to hide/show login/logout data.
 * @param logedIn @c true if the user logged in, @c false if he logged out
 * @param userName The name of the user when logged in, field is optional
 */
function switchUserInformation(logedIn, userName) {
    if (logedIn) {
        if (userName) {
            $("#userInformationLogedIn span").first().text(userName);
        }
        $("#userInformationLogedIn").show();
        $("#userInformationLogedOut").hide();
    } else {
        $("#userInformationLogedIn").hide();
        $("#userInformationLogedOut").show();
    }
}

/**
 * Shows and resets the login dialog.
 */
function showLoginDialog() {
    $("#ajax_j_username").val("");
    $("#ajax_j_password").val("");
    $("#ajaxLoginStatus").hide();
    $('#ajaxLoginDialog').dialog('open');
}

/**
 * Performs authentication through AJAX by serializing the ajaxLoginForm.
 * On success the login dialog is closed and the login event is fired.
 * On failure the error message is displayed.
 */
function authAjax() {
    $.post(createURI("j_spring_security_check"), $("#ajaxLoginForm").serialize(), function(data) {
        if (data.success) {
            $("#ajaxLoginDialog").dialog('close');
            $(document).trigger("login", data.username)
        } else if (data.error) {
            $("#ajaxLoginStatus").html(data.error);
            $("#ajaxLoginStatus").show();
        }
    });
}

/**
 * Performs logout through AJAX.
 * On success the logout event is fired.
 */
function logout() {
    $.ajax({ url: createURI("logout"),
        success: function(data, textStatus, jqXHR) {
            $(document).trigger("logout");
        }
    });
}

/**
 * Creates a URI to be used in a href or src HTML attribute.
 * @param path The path
 */
function createURI(path) {
    return "/" + $.appName + "/" + path;
}

/**
 * Same as g.createLink.
 * @param controller The name of the grails controller
 * @param action The optional action
 * @param id The optional id
 */
function createLink(controller, action, id) {
    var path = controller;
    if (action != undefined) {
        path += "/" + action;
        if (id != undefined) {
            path += "/" + id;
        }
    }
    return createURI(path);
}

/**
 * Creates the data table used in the model overview page.
 */
function createModelDataTable() {
    if ($('#modelTable').length == 0) {
        return;
    }
    $('#modelTable').dataTable({
        // TODO: in future it might be interesting to allow filtering
        bFilter: false,
        bProcessing: true,
        bServerSide: true,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        sAjaxSource: createLink('model', 'dataTableSource'),
        // TODO: move function into an own method
        "fnRowCallback": function( nRow, aData, iDisplayIndex, iDisplayIndexFull ) {
            // first column is link to model
            $('td:eq(0)', nRow).html( '<a href="' + createLink("model", "show", aData[0]) + '">' + aData[0] + '</a>' );
            // fifth column contains a download link
            $('td:eq(4)', nRow).html(aData[4] + '&nbsp;<a href="' + createLink('model', 'download', aData[0]) + '">' + i18n.model.list.download + '</a>');
            return nRow;
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
    $(document).bind("login", function(event) {
        $('#modelTable').dataTable().fnDraw();
    });
}

/**
 * Global document initialization.
 * Connects all the global events like login/logout.
 */
$(document).ready(function() {
    $(document).bind("logout", function() {
        switchUserInformation(false);
    });
    $(document).bind("login", function(event, username) {
        switchUserInformation(true, username);
    });
    // create Ajax Login Dialog
    // TODO: maybe delay till first time used?
    $("#ajaxLoginDialog").dialog({
        autoOpen: false,
        width: 400, // need a slightly larger dialog
        title: i18n.login.authenticate,
        buttons: [
            {
                text: i18n.login.authenticate,
                click: authAjax
            },
            {
                text: i18n.login.cancel,
                click: function() { $(this).dialog("close")}
            }
        ]
    });
    createModelDataTable();
});
