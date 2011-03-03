<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="error.404.title"/></title>
    </head>
    <body>
        <div class="body">
            <div id="status-code" style="display: none">404</div>
            <div id="resource" style="display: none">${resource}</div>
            <div class="commenthead">
                <g:message code="error.404.title"/>
            </div>
            <div class="commentbody">
                <g:message code="error.404.explanation" args="[resource]"/>
            </div>
        </div>
  </body>
</html>
