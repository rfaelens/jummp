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
 * Creates a URI to be used in a href or src HTML attribute.
 * @param path The path
 */
function createURI(path) {
    return "/" + $.appName + "/" + path;
};

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
});