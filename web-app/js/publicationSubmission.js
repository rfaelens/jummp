var authors = [];
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
    $("#authorList > option").each(function() {
        if (this.value.toLowerCase() === id.toLowerCase()) {
            alreadyPresent=true
        }
    });
    if (!alreadyPresent) {
        $('#authorList')
            .append($('<option>', { value : id })
            .text(fullName));
        authors.push(id);
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
    /* Basic initialisations */
    $("#synopsis").width("94%");
    $("#affiliation").width("94%");
    $("#authorList").width("97%");
    $("#authorList").change(function() {
        var value = $(this).val();
        var authorDetail = value.split('<init>');
        if (authorDetail[0]) {
            $("#newAuthorName").val(authorDetail[0]);
        }
        if (authorDetail[1] === 'no_orcid') {
            $("#newAuthorOrcid").val(null);
        } else {
            $("#newAuthorOrcid").val(authorDetail[1]);
        }
        if (authorDetail[2] === 'no_institution_provided') {
            $("#newAuthorInstitution").val(null);
        } else {
            $("#newAuthorInstitution").val(authorDetail[2]);
        }
    });
    $('#addButton').click(function() {
        addText()
        setHiddenFieldValue()
    });
    $('#deleteButton').click(function() {
        console.log("Delete");
        var value = $("#authorList").val();
        var fullName = value.split('<init>')[0];
        console.log(fullName);
        var selectedItem = $("#authorList option:selected").val();
        console.log(selectedItem);
        console.log(authors);
        //var res = jQuery.inArray(selectedItem,authors);
        var res = authors.indexOf(selectedItem);
        console.log("Position: ", res);
        if (res >=0 || res < authors.size) {
            console.log("Found: ", res);
            var au = authors.splice(res,1);
            console.log(authors);
        } else {
            console.log("Not Found");
        }
        $("#authorList option:selected").remove();
        $('#newAuthorName').val("")
        $('#newAuthorOrcid').val("")
        $('#newAuthorInstitution').val("")
        $('#authorFieldTotal').val(authors);
        console.log(authors);
        console.log($('#authorFieldTotal').val());
    });
});

function setHiddenFieldValue() {
    var text=""
    $('#authorList > option').each(function(i, option) {
        text = text+ $(option).val();
    });
    $('#authorFieldTotal').val(authors);
}

