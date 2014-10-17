/* The notification system depends on including notificationDiv template in the 
page (ensuring the presence of the notification div)
 */
function scheduleHide() {
	setTimeout(function(){
		$(".flashNotificationDiv").fadeOut("slow", function () {
		$(".flashNotificationDiv").hide();
	}); }, 4000);

}

function hideNow() {
		$(".flashNotificationDiv").hide();
}

function showNotification(message) {
	$(".flashNotificationDiv").show()
	$(".flashNotificationDiv").text(message)
}

function pollForNotifications(url, period) {
	setInterval(function() {
		$.get( url, function( data ) {
				if (data != "0") {
					alert( "You have "+data+" notifications" );
				}
		});
	}, period);
}