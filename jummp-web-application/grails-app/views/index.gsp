<html>
    <head>
        <title>Welcome to JUMMP</title>
        <meta name="layout" content="main" />
    </head>
    <body>
        <div id="body" class="ui-widget">
            <a href="#" onclick="showModelList()" style="display: none" id="show-model-list-link"><g:message code="model.list.goto"/></a>
            <a href="#" onclick="showUploadModel()" style="display: none"><g:message code="model.upload.goto"/></a>
            <g:javascript>
            $(document).ready(function() {
                $("#body a").button();
                $("#body a").show();
            });
            </g:javascript>
        </div>
    </body>
</html>
