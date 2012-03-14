<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Model</title>
        <meta name="layout" content="main" />
        <r:require module="showModels"/>
    </head>
    <body activetab="search">
    <r:script>
$(function() {
    $.jummp.showModels.showOverlay("${g.createLink(controller: 'search', action: 'model', id: id)}", function() {
        $("body").block();
        window.location.pathname = $.jummp.createURI("");
    });
});
    </r:script>
    </body>
</html>
