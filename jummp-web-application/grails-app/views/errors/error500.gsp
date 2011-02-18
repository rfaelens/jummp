<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="error.500.title"/></title>
    </head>
    <body>
        <div class="body">
            <div id="status-code" style="display: none">500</div>
            <div id="error-code" style="display: none">${code}</div>
            <div class="commenthead">
                <g:message code="error.500.title"/>
            </div>
            <div class="commentbody">
                <g:message code="error.500.explanation" args="${[code]}"/> 
            </div>
        </div>
  </body>
</html>
