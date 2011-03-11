<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main" />
        <title><g:message code="error.403.title"/></title>
    </head>
    <body>
        <div id="body" class="ui-widget">
            <div id="status-code" style="display: none">403</div>
            <div id="authenticated" style="display: none">${authenticated}</div>
            <g:javascript>
            $(document).ready(function() {
                showErrorMessage(i18n.error.denied);
                <g:if test="${!authenticated}">
                    switchUserInformation(false);
                    showLoginDialog();
                </g:if>
            })
            </g:javascript>
        </div>
  </body>
</html>
