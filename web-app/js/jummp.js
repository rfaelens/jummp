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
 * Loads a new view for the #body element through AJAX.
 * @param url The URL from where to load the view
 * @param loadCallback A callback to execute after successfully updating the view.
 * @param callbackData Additional data to be passed to the callback
 */
function loadView(url, loadCallback, callbackData) {
    //$('div').filter('#main').block();
    //alert(jQuery('div').filter('#main').length + ' main divs');
    alert('url: ' + url + 'loadCallback: ' + loadCallback + ' callbackData: ' + callbackData)
    //$("#body").block();
    $.ajax({
        url: url,
        dataType: 'HTML',
        type: 'GET',
        cache: 'false',
        success: function(data) {
            alert('here!')
            $("#body").unblock();
            clearErrorMessages();
            $("#body").html(data);
            loadCallback(data, callbackData);
        },
        error: function(jqXHR) {
            $("#body").unblock();
        },
        statusCode: {
            400: handler400,
            403: handler403,
            404: handler404,
            500: handler500
        }
    });
}