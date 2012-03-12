// TODO: create proper links
$.i18n.properties({
    name: 'messages',
    path: '/jummp/js/i18n/',
    mode: "map"
});

$.jummp = new Object();

$.jummp.message = function(message, warning) {
    if (!message) {
        return;
    }
    var classes = "message";
    if (warning) {
        classes += " warning";
    }
    var button = $("<div><button></button></div>");
    $("button", button).click(function() {
        $(this).parent().parent().remove();
    });
    var divCode = $("<div class=\"" + classes + "\"><p>" + message + "</p></div>");
    button.appendTo(divCode);
    $("#infoBox").append(divCode);
};

$.jummp.infoMessage = function(message) {
    this.message(message, false);
};

$.jummp.warningMessage = function(message) {
    this.message(message, true);
};

/**
 * Same as g.createLink.
 * @param controller The name of the grails controller
 * @param action The optional action
 * @param id The optional id
 */
$.jummp.createLink = function(controller, action, id) {
    var path = controller;
    if (action != undefined) {
        path += "/" + action;
        if (id != undefined) {
            path += "/" + id;
        }
    }
    return $.jummp.createURI(path);
};

/**
 * Creates a URI to be used in a href or src HTML attribute.
 * @param path The path
 */
$.jummp.createURI = function(path) {
    return "/" + $.appName + "/" + path;
}

/**
 * Removes all shown error messages of the site error message div and hides it.
 * This method can be used to clear the current state before adding new error
 * messages with showErrorMessage().
 */
$.jummp.clearErrorMessages = function() {
    $("#site-error-messages ul li").remove();
    $("#site-error-messages").hide();
}

$.jummp.handler400 = function(jqXHR) {
    clearErrorMessages();
    showErrorMessage(jqXHR.responseText);
}

/**
 * Callback for a 403 response.
 * Shows the error message and expects as responseText either @c true for authenticated users or @c false otherwise.
 * @param jqXHR The jqXHR object passed by ajax function.
 */
$.jummp.handler403 = function(jqXHR) {
    clearErrorMessages();
    showErrorMessage(i18n.error.denied);
    if (jqXHR.responseText == "false") {
        switchUserInformation(false);
        showLoginDialog();
    }
}

/**
 * Callback for a 404 response.
 * Expects as responseText the resource which could not be found.
 * @param jqXHR The jqXHR object passed by ajax function.
 */
$.jummp.handler404 = function(jqXHR) {
    clearErrorMessages();
    showErrorMessage(i18n.error.notFound.replace(/_CODE_/, jqXHR.responseText));
}

/**
 * Callback for a 500 response.
 * Expects as responseText the error code on the server.
 * @param jqXHR The jqXHR object passed by ajax function.
 */
$.jummp.handler500 = function(jqXHR) {
    clearErrorMessages();
    showErrorMessage(i18n.error.unexpected.replace(/_CODE_/, jqXHR.responseText));
}

$(function() {
    $("#loginLogout button.logout").click(function() {
        window.location.pathname = $.jummp.createURI("logout");
    });
    $("#loginLogout button.login").click(function() {
        window.location.pathname = $.jummp.createLink("login", "auth");
    });
});
