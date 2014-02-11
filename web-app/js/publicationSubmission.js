function addText() {
	if ($('#newAuthorName').val()) {
	var fullName=$('#newAuthorName').val();
	var orcid = $('#newAuthorOrcid').val();
	if (orcid.length==0) {
		orcid="no_orcid";
	}
	var institution = $('#newAuthorInstitution').val()
	if (institution.length==0) {
		institution="no_institution_provided";
	}
	var id=fullName+"<init>"+orcid+"<init>"+institution
	var alreadyPresent=false
	alert(orcid+".."+institution)
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
		showNotification("An author by that name is already added to the publication.")
	}
	$('#newAuthorName').val("")
	$('#newAuthorOrcid').val("")
	$('#newAuthorInstitution').val("")
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

