$.jummp.gotree = {};

$.jummp.gotree.load = function() {
    "use strict";
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
                $.jummp.showModels.showOverlay($.jummp.createLink("model", "model", node.data.modelId));
            }
        }
    });
    $("#gotree-filter").autocomplete({
        minLength: 3,
        html: true,
        source: function(req, add) {
            // retrieves the matching search results and adds them to the list
            $.getJSON($.jummp.createLink("gotree", "search", req.term), function(data) {
                var i;
                for (i=0; i<data.length; i++) {
                    data[i].label = "<strong>" + data[i].goTerm + "</strong><br/>\n" + data[i].goId;
                }
                add(data);
            });
        },
        select: function(e, ui) {
            // when a search result is selected we contact the server to get the path for the node
            $.getJSON($.jummp.createLink("gotree", "path", ui.item.id), function(data) {
                // based on the path we load all nodes so that the tree can be expanded
                $("#gotree").dynatree("getTree").loadKeyPath(data.path, function(node, status) {
                    if (status === "loaded") {
                        node.expand();
                    } else if (status === "ok") {
                        node.activate();
                        node.expand();
                    }
                });
            });
        }
    });
};
