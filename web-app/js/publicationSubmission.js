function addText() {
	if ($('#newAuthorLastName').val()) {
	var lastName=$('#newAuthorLastName').val()
	var fullName=lastName
	var id=lastName+"<init>"
	if ($('#newAuthorInitials').val()) {
			var initials=$('#newAuthorInitials').val()
			id=id+initials
			fullName=initials+". "+lastName
	}
	var alreadyPresent=false
	$("#authorList > option").each(function() {
		if (this.value === id) {
			alreadyPresent=true
		}
	});
	if (!alreadyPresent) {
		$('#authorList')
			.append($('<option>', { value : id })
			.text(fullName));
			setHiddenFieldValue();
	}
	else {
		alert("An author by that name is already added to the publication.")
	}
	$('#newAuthorInitials').val("")
	$('#newAuthorLastName').val("")
}
}

$(document).ready(function () {
		$('#addButton').click(function() {
			addText()
		});
		setHiddenFieldValue()
});

function setHiddenFieldValue() {
	var text=""
	$('#authorList > option').each(function(i, option) {
			text=text+"!!author!!"+$(option).val();
	});
	$('#authorFieldTotal').val(text);
}

