$.jummp.gotree = new Object();

$.jummp.gotree.load = function() {
    $("#gotree").dynatree({
        imagePath: $.jummp.createURI("static/css/dynatree/"),
        initAjax: {
            url: $.jummp.createLink("gotree", "level", 0)
        },
        onLazyRead: function(node) {
            node.appendAjax({
                url: $.jummp.createLink("gotree", "level", node.data.goid)
            });
        },
        onActivate: function(node) {
            if (!node.data.isFolder) {
                $.jummp.showModels.showOverlay($.jummp.createLink("search", "model", node.data.modelId));
            }
        }
    });
};
