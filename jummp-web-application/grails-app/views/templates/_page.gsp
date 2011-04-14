<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Welcome to JUMMP</title>
        <meta name="layout" content="main" />
        <g:javascript>
        $(document).ready(function() {
            <g:if test="${data}">
                loadView("${link}", ${callback}, "${data}");
            </g:if>
            <g:else>
                loadView("${link}", ${callback});
            </g:else>
        });
        </g:javascript>
    </head>
    <body>
        <div id="body" class="ui-widget"></div>
    </body>
</html>
