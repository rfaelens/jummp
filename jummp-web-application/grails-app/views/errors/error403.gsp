<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="error.403.title"/></title>
    </head>
    <body>
        <div class="body">
            <div id="status-code" style="display: none">403</div>
            <div id="authenticated" style="display: none">${authenticated}</div>
            <div class="commenthead">
                <g:message code="error.403.title"/>
            </div>
            <div class="commentbody">
                <g:message code="error.403.explanation"/> 
            </div>
        </div>
  </body>
</html>
