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

$(function() {
    $("#loginLogout button.logout").click(function() {
        window.location.pathname = $.jummp.createURI("logout");
    });
    $("#loginLogout button.login").click(function() {
        window.location.pathname = $.jummp.createLink("login", "auth");
    });
});
