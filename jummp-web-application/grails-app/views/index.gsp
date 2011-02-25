<html>
    <head>
        <title>Welcome to JUMMP</title>
        <meta name="layout" content="main" />
    </head>
    <body>
        <div id="body">
            <a href="#" onclick="showModelList()" style="display: none" id="show-model-list-link"><g:message code="model.list.goto"/></a>
            <g:javascript>
            $(document).ready(function() {
                $("#show-model-list-link").button();
                $("#show-model-list-link").show();
            });
            </g:javascript>
        </div>
    </body>
</html>
