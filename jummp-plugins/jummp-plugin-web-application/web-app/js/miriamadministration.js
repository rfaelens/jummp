$.jummp.miriam = new Object();

$.jummp.miriam.init = function() {
    $("#miriam form").submit($.jummp.miriam.updateResources);
    $("#miriam-update form").submit($.jummp.miriam.updateMiriamData);
    $("#miriam-model-update form").submit($.jummp.miriam.updateModels);
};

$.jummp.miriam.updateResources = function() {
    $.ajax({
        url: $.jummp.createLink("miriam", "updateResources"),
        dataType: 'json',
        data: {
            miriamUrl: "Foo",//$("#miriam-update-miriam-url").val(),
            force: $("#miriam-update-force").prop("checked")
        },
        success: function(data) {
            if (data.success) {
                $.jummp.infoMessage($.i18n.prop("miriam.update.success"));
            } else if (data.error) {
                if (data.error != true) {
                    $.jummp.warningMessage(data.error);
                }
                $.jummp.warningMessage(data.miriamUrl);
            }
        }
    });
    return false;
};

$.jummp.miriam.updateMiriamData = function() {
    $.ajax({
        url: $.jummp.createLink("miriam", "updateMiriamData"),
        dataType: 'json',
        success: function(data) {
            if (data.success) {
                $.jummp.infoMessage($.i18n.prop("miriam.data.update.success"));
            } else if (data.error) {
                if (data.error != true) {
                    $.jummp.warningMessage(data.error);
                }
            }
        }
    });
    return false;
};

$.jummp.miriam.updateModels = function() {
    $.ajax({
        url: $.jummp.createLink("miriam", "updateModels"),
        dataType: 'json',
        success: function(data) {
            if (data.success) {
                $.jummp.infoMessage($.i18n.prop("miriam.model.update.success"));
            } else if (data.error) {
                if (data.error != true) {
                    $.jummp.warningMessage(data.error);
                }
            }
        }
    });
    return false;
};
