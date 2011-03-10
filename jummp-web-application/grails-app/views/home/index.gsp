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
    case "UPLOADMODEL":
%>
           showUploadModel();
<%
        break
    case "ADDREVISION":
%>
            showModel("${params.id}", "#modelTabs-addRevision");
<%
        break
    case "THEMES":
%>
            showThemeSelection();
<%
        break
    case "USER":
%>
            showUserInfo();
<%
        break
    case "USERADMINLIST":
%>
            showUserList();
<%
        break
    case "REGISTER":
%>
            showRegisterView();
<%
        break
    case "VALIDATE":
%>
            showValidateRegistrationView("${params.id}");
<%
        break
    case "RESETPASSWORD":
%>
            showResetPasswordView("${params.id}");
<%
        break
    }
%>
        });
        </g:javascript>
    </head>
    <body>
        <div id="body" class="ui-widget"></div>
    </body>
</html>