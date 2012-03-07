$.jummp.gotree = new Object();

$.jummp.gotree.load = function() {
    $("#gotree").dynatree({
        imagePath: "/jummp/static/css/dynatree/",
        initAjax: {
            url: "level/0/"
        },
        onLazyRead: function(node) {
            node.appendAjax({
                url: "level/" + node.data.goid + "/"
            });
        },
        onActivate: function(node) {
            if (!node.data.isFolder) {
                $.jummp.showModels.showOverlay(createLink("search", "model", node.data.modelId));
            }
        }
    });
};
