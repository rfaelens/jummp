$.jummp = {
    i18n: {
        dataTables: {
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
    }
};
/**
 * Loads a new view for the #body element through AJAX.
 * @param url The URL from where to load the view
 * @param loadCallback A callback to execute after successfully updating the view.
 * @param callbackData Additional data to be passed to the callback
 */
function loadView(url, loadCallback, callbackData) {
    $("#body").block();
    $.ajax({
        url: url,
        dataType: 'HTML',
        type: 'GET',
        success: function(data) {
            $("#body").unblock();
            clearErrorMessages();
            var json = null;
            try {
                json = $.parseJSON(data);
            } catch (e) {
                // ignore - this is expected for the case that HTML is retrieved
            }
            if (handleError(json)) {
                // TODO: with jquery 1.5 should be handled by status code function
                return;
            }
            $("#body").html(data);
            loadCallback(data, callbackData);
        },
        error: function(jqXHR) {
            $("#body").unblock();
            handleError($.parseJSON(jqXHR.responseText));
        }
    });
}

/**
 * Submits the @p form to the @p url via AJAX.
 * If the call is successful the @p callback is invoked with the retrieved json data as parameter.
 * @param form JQuery object identifying the form to submit
 * @param url The URL where to submit to
 * @param callback The callback object to invoke
 */
function submitForm(form, url, callback) {
    form.block();
    form.ajaxSubmit({
        type: 'POST',
        url: url,
        dataType: 'json',
        success: function (data) {
            form.unblock();
            if (handleError(data)) {
                // TODO: with jquery 1.5 should be handled by status code function
                return;
            }
            clearErrorMessages();
            callback(data);
        },
        error: function(jqXHR) {
            form.unblock();
            handleError($.parseJSON(jqXHR.responseText));
        }
    });
}

/**
 * Submits the @p form to the @p url via AJAX.
 * The method is like @link submitForm with the difference that it can handle file uploads.
 * Therefore the iframe option is used which causes the server to think it is not an AJAX request.
 * The server needs to wrap the JSON response in a textarea. In case of errors like 403 a web page
 * is returned wich is parsed correctly and ensuring the correct functionality.
 * @param form JQuery object identifying the form to submit
 * @param url The URL where to submit to
 * @param callback The callback object to invoke
 */
function submitFormWithFile(form, url, callback) {
    form.block();
    form.ajaxSubmit({
        type: 'POST',
        url: url,
        // needs to be an iframe as we send a file
        iframe: true,
        dataType: 'json',
        success: function(data) {
            form.unblock();
            if (handleError(data)) {
                // TODO: with jquery 1.5 should be handled by status code function
                return;
            }
            clearErrorMessages();
            callback(data);
        },
        error: function(jqXHR) {
            form.unblock();
            // the form is not submitted as AJAX (file upload) because of that we receive an html page
            // we need to extract the status code and error code from the html
            // and construct a proper object to pass to handleError()
            var response = $(jqXHR.responseText);
            var errorCode = $("#error-code", response).text();
            var statusCode = parseInt($("#status-code", response).text());
            var authenticated = ($("#authenticated", response).text() == "true");
            if (isNaN(statusCode) && jqXHR.responseXML) {
                statusCode = parseInt($("#status-code", $(jqXHR.responseXML)).text());
                authenticated = ($("#authenticated", $(jqXHR.responseXML)).text() == "true");
            }
            handleError({error: statusCode, code: errorCode, authenticated: authenticated});
        }
    });
}

/**
 * Updates the user information panel to hide/show login/logout data.
 * @param logedIn @c true if the user logged in, @c false if he logged out
 * @param userName The name of the user when logged in, field is optional
 */
function switchUserInformation(logedIn, userName) {
    if (logedIn) {
        if (userName) {
            var url = createLink('user', 'index');
            $("#userInformationLogedIn span").first().html("<a href=\"#\" onclick=\"loadView('" + url + "', loadShowUserInfoCallback)\">" + userName + "</a>");
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
    $.ajax({
        url: createURI("j_spring_security_check"),
        type: 'POST',
        data: $("#ajaxLoginForm").serialize(),
        success: function(data) {
            if (data.success) {
                $("#ajaxLoginDialog").dialog('close');
                $(document).trigger("login", data.username)
            } else if (data.error) {
                $("#ajaxLoginStatus").html(data.error);
                $("#ajaxLoginStatus").show();
            }
        },
        error: function(jqXHR) {
            handleError($.parseJSON(jqXHR.responseText));
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
 * Loads the User Registration view by AJAX and shows it in a dialog.
 */
function showRegisterView() {
    $.ajax({
        url: createLink("register", "index"),
        dataType: "html",
        success: function(data) {
            $(data).dialog({
                width: 600, // need a slightly larger dialog
                title: i18n.user.register.title,
                buttons: [
                    {
                        text: i18n.ui.button.register,
                        click: register
                    },
                    {
                        text: i18n.login.cancel,
                        click: function() {
                            $(this).dialog("close");
                        }
                    }
                ],
                close: function() {
                    $(this).remove();
                }
            });
        },
        error: function(jqXHR) {
            handleError($.parseJSON(jqXHR.responseText));
        }
    });
}

/**
 * Callback for user registration.
 * In success case the registration dialog will be closed.
 */
function register() {
    var dialog = $(this);
    $.ajax({
        url: createLink("register", "register"),
        type: 'POST',
        data: $("#registerForm").serialize(),
        success: function(data) {
            if (data.success) {
                showInfoMessage(i18n.user.register.success, 20000);
                dialog.dialog("close");
            } else if (data.error) {
                $("#registerStatus ul li", dialog).remove();
                var errorContainer = $("#registerStatus", dialog);
                if (data.error != true) {
                    showMessage(data.error, null, errorContainer);
                }
                showMessage(data.username, null, errorContainer);
                showMessage(data.password, null, errorContainer);
                showMessage(data.verifyPassword, null, errorContainer);
                showMessage(data.email, null, errorContainer);
                showMessage(data.userRealName, null, errorContainer);
                setErrorState("#register-form-username", data.username);
                setErrorState("#register-form-password", data.password);
                setErrorState("#register-form-verifyPassword", data.verifyPassword);
                setErrorState("#register-form-email", data.email);
                setErrorState("#register-form-name", data.userRealName);
            }
        },
        error: function(jqXHR) {
            handleError($.parseJSON(jqXHR.responseText));
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
 * Loads the view to show a Model and replaces.
 * @param id The id of the Model to show
 * @param tabIndex Optional selector for tab index to switch to after the tab view has been loaded
 */
function showModel(id, tabIndex) {
    loadView(createLink("model", "show", id), loadModelTabCallback, tabIndex);
}

/**
 * Callback for changing a boolean attribute in the User Table
 * @param userId The User Id
 * @param field The id of the checkbox
 * @param target The URL target for the AJAX call.
 */
function changeUser(userId, field, target) {
    $("#userTable").block();
    $.ajax({
        url: createLink("userAdministration", target, userId),
        dataType: 'json',
        data: {value: $("#" + field).attr("checked")},
        success: function(data) {
            $("#userTable").unblock();
            clearErrorMessages();
            if (data.success) {
                showInfoMessage(i18n.user.editSuccess, 20000);
            } else if (data.error) {
                showErrorMessage(data.message);
            } else {
                showInfoMessage(i18n.user.unchanged, 20000);
            }
            // redraw the dataTable to reset all changes
            $('#userTable').dataTable().fnDraw();
        },
        error: function(jqXHR) {
            $("#userTable").unblock();
            handleError($.parseJSON(jqXHR.responseText));
        }
    });
}

/**
 * Creates HTML markup for a hyperlink to citexplore referencing a PubMed Id.
 * The hyperlink has a class "tooltip", a title and rel attribute referencing a tooltip.
 * The following information from the JSON structure is used:
 * @li link: The PubMed ID
 * @li compactTitle: The title of the publication
 * @param publication JSON object describing the publication
 * @param model The id of the model
 */
function createPubMedLink(publication, model) {
    return createPublicationLink(publication, model, 'http://www.ebi.ac.uk/citexplore/citationDetails.do?dataSource=MED&externalId=' + publication.link);
}

/**
 * Creates HTML markup for a hyperlink to a DOI resource.
 * The hyperlink has a class "tooltip", a title and rel attribute referencing a tooltip.
 * The following information from the JSON structure is used:
 * @li link: The DOI link
 * @li compactTitle: The title of the publication
 * @param publication JSON object describing the publication
 * @param model The id of the model
 */
function createDoiLink(publication, model) {
    return createPublicationLink(publication, model, 'http://dx.doi.org/' + publication.link);
}

/**
 * Creates HTML markup for a hyperlink referencing a publication.
 * The hyperlink has a class "tooltip", a title and rel attribute referencing a tooltip.
 * The following information from the JSON structure is used:
 * @li link: The PubMed ID
 * @li compactTitle: The title of the publication
 * @param publication JSON object describing the publication
 * @param model The id of the model
 * @param target The link target
 */
function createPublicationLink(publication, model, target) {
    return '<a class="tooltip" target="_blank" href="' + target + '" title="' + i18n.model.summary.referencePublication + '" rel="' + createLink('model', 'publication', model) + '">' + publication.compactTitle + '</a>';
}

/**
 * Enable/Disable DOM element(s).
 * Adds/Removes the disabled attribute and sets appropriate CSS classes.
 * @param selector The selector string to identify the element(s)
 * @param enable @c true to enable it, @c false to disable it.
 */
function enableElement(selector, enable) {
    var element = $(selector);
    element.attr("disabled", !enable);
    if (enable) {
        element.removeClass("ui-state-disabled");
    } else {
        element.addClass("ui-state-disabled");
    }
}

/**
 * Sets the state of widgets to error or removes the error state.
 * If the widget is next to a span element with an alert icon, the alert icon is
 * shown and the @p error is used as the title element.
 * @param selector Selector string to identify the widget(s)
 * @param error Error message, if @c null, the error state is withdrawn.
 */
function setErrorState(selector, error) {
    if (error) {
        $(selector).addClass("ui-state-error");
        if (selector[0] == '#') {
            $("label[for=\"" + selector.substring(1) + "\"]").addClass("ui-state-error-text");
        }
        var icon = $(selector).next("span.ui-icon-alert");
        if (icon) {
            icon.attr("title", error);
            icon.show();
        }
    } else {
        $(selector).removeClass("ui-state-error");
        if (selector[0] == '#') {
            $("label[for=\"" + selector.substring(1) + "\"]").removeClass("ui-state-error-text");
        }
        $(selector).next("span.ui-icon-alert").hide();
    }
}

/**
 * Removes all shown error messages of the site error message div and hides it.
 * This method can be used to clear the current state before adding new error
 * messages with showErrorMessage().
 */
function clearErrorMessages() {
    $("#site-error-messages ul li").remove();
    $("#site-error-messages").hide();
}

/**
 * Adds @p message to the site info message div and shows it.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 */
function showInfoMessage(messages, timeout) {
    showMessage(messages, timeout, $("#site-info-messages"));
}

/**
 * Adds an error message to the site error message div and shows it.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 */
function showErrorMessage(messages, timeout) {
    showMessage(messages, timeout, $("#site-error-messages"));
}

/**
 * Adds @p message to the @p container and shows the @p container.
 * @param messages The message to show, String or Array of Strings
 * @param timeout Time in msec until the message is automatically removed. If not specified the message does not get removed.
 * @param container A jQuery object identifying the container, e.g. the site-error-messages or site-info-messages
 */
function showMessage(messages, timeout, container) {
    if (!messages) {
        return;
    }
    if (!(messages instanceof Array)) {
        messages = new Array(messages);
    }
    for (var i=0; i<messages.length; i++) {
        var message = messages[i];
        if (!message) {
            continue;
        }
        var error = $("<li>" + message + "</li>");
        // add close button to the error message
        var close = $("<a href=\"#\"></a>").button({text: false});
        close.button("option", "icons", {primary:'ui-icon-closethick'});
        error.append(close);
        // close button removes the error and if it was the last one hides the container
        close.click(function() {
            var parent = $(this).parent();
            parent.fadeOut("fast", function() {
                parent.remove();
                $("span[rel=icon]", container).position({my: "left", at: "left", of: container});
            });
            if ($("ul li", container).length == 1) {
                container.fadeOut("fast");
            }
        });
        if (timeout) {
            setTimeout(function() {close.trigger("click");}, timeout);
        }
        $("ul", container).append(error);
        close.hide();
    }

    // show the errors
    container.fadeIn("fast");
    $("span[rel=icon]", container).position({my: "left", at: "left", of: container});
    var closeButtons = $("ul li a", container);
    for (i=0; i<closeButtons.length; i++) {
        var button = $(closeButtons[i]);
        button.show();
        button.position({my: "right", at: "right", of: $(closeButtons[i]).parent(), collision: "flip"});
    }
}

/**
 * This method checks whether the passed in @p data describes a returned
 * error. If that is the case it will show an error message and return @c true.
 * @param data The JSON object
 * @returns @c true if the request contains an error, @c false otherwise
 * @todo With JQuery 1.5 we should use the statusCode property
 */
function handleError(data) {
    if (data == null) {
        return false;
    }
    if (data.error && typeof(data.error) == "number") {
        clearErrorMessages();
        var errorMessage = "";
        switch (data.error) {
            case 403:
                errorMessage = i18n.error.denied;
                if (data.authenticated != undefined && data.authenticated == false) {
                    switchUserInformation(false);
                    showLoginDialog();
                }
                break;
            case 404:
                errorMessage = i18n.error.notFound.replace(/_CODE_/, data.resource);
                break;
            case 500:
                errorMessage = i18n.error.unexpected.replace(/_CODE_/, data.code);
                break;
        }
        showErrorMessage(errorMessage);
        return true;
    }
    return false;
}

/**
 * Global document initialization.
 * Connects all the global events like login/logout.
 */
$(document).ready(function() {
    $(document).bind("logout", function() {
        showInfoMessage(i18n.logout.successful, 20000);
        switchUserInformation(false);
    });
    $(document).bind("login", function(event, username) {
        showInfoMessage(i18n.login.successful, 20000);
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
    $("#ajaxLoginForm table input").keyup(function(event) {
        if (event.keyCode == 13) {
            authAjax();
        }
    });
    $("#ajaxLoginForm").submit(function() { return false; });
    $("#model-upload-form").ajaxForm();
});
