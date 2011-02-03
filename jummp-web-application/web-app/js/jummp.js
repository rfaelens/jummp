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