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
        onDblClick: function(node) {
            if (!node.data.isFolder) {
                // TODO: show overlay
            }
        }
    });
};
