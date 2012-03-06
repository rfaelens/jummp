$.jummp = new Object();

$.jummp.message = function(message, warning) {
    if (!message) {
        return;
    }
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
 * Creates a URI to be used in a href or src HTML attribute.
 * @param path The path
 */
function createURI(path) {
   // console.log("$.appName: " + $.appName)
    return "/" + $.appName + "/" + path;
}
