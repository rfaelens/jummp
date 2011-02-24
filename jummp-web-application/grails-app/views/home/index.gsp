<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Welcome to JUMMP</title>
        <meta name="layout" content="main" />
        <g:javascript>
        $(document).ready(function() {
<%
    switch (params.redirect) {
    case "MODELLIST":
%>
            showModelList();
<%
        break
    case "SHOWMODEL":
%>
           showModel("${params.id}");
<%
        break
    }
%>
        });
        </g:javascript>
    </head>
    <body>
        <div id="body"></div>
    </body>
</html>