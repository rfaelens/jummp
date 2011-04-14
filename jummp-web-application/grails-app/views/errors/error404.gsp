<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="error.404.title"/></title>
    </head>
    <body>
        <div id="body" class="ui-widget">
            <div id="status-code" style="display: none">404</div>
            <div id="resource" style="display: none">${resource}</div>
            <g:javascript>
            $(document).ready(function() {
                showErrorMessage(i18n.error.notFound.replace(/_CODE_/, "${resource}"));
            });
            </g:javascript>
        </div>
  </body>
</html>
