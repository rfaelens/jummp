<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Gene Ontology Tree</title>
        <meta name="layout" content="main" />
        <link rel="stylesheet" href="${resource(dir:'css/dynatree', file:'ui.dynatree.css')}" type="text/css"/>
        <r:require module="jquery-ui"/>
        <r:require module="gotree"/>
    </head>
    <body>
        <div id="gotree"></div>
        <r:script>
$(function() {
    $.jummp.gotree.load();
});
        </r:script>
    </body>
</html>
