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
            loadView("${g.createLink(controller: 'model', action: 'index')}", loadModelListCallback);
<%
        break
    case "SHOWMODEL":
%>
           loadView("${g.createLink(controller: 'model', action: 'show', id: params.id)}", loadModelTabCallback);
<%
        break
    case "UPLOADMODEL":
%>
           loadView("${g.createLink(controller: 'model', action: 'upload')}", loadUploadModelCallback);
<%
        break
    case "ADDREVISION":
%>
            loadView("${g.createLink(controller: 'model', action: 'show', id: params.id)}", loadModelTabCallback, "#modelTabs-addRevision");
<%
        break
    case "THEMES":
%>
            loadView("${g.createLink(controller: 'themeing', action: 'themes')}", loadThemeSelectionCallback);
<%
        break
    case "USER":
%>
            loadView("${g.createLink(controller: 'user', action: 'index')}", loadShowUserInfoCallback);
<%
        break
    case "USERADMINLIST":
%>
            loadView("${g.createLink(controller: 'userAdministration', action: 'index')}", loadUserListCallback);
<%
        break
    case "USERADMINSHOW":
%>
            loadView("${g.createLink(controller: 'userAdministration', action: 'show', id: params.id)}", loadAdminUserCallback);
<%
        break
    case "USERADMINREGISTER":
%>
            loadView("${g.createLink(controller: 'userAdministration', action: 'register')}", loadAdminRegisterCallback);
<%
        break
    case "REGISTER":
%>
            showRegisterView();
<%
        break
    case "VALIDATE":
%>
            loadView("${g.createLink(controller: 'register', action: 'validate', id: params.id)}", loadValidateRegistrationCallback);
<%
        break
    case "CONFIRMREGISTRATION":
%>
            loadView("${g.createLink(controller: 'register', action: 'confirmRegistration', id: params.id)}", loadConfirmRegistrationCallback);
<%
        break
    case "RESETPASSWORD":
%>
            loadView("${g.createLink(controller: 'user', action: 'resetPassword', id: params.id)}", loadResetPasswordCallback);
<%
        break
    case "PASSWORDFORGOTTEN":
%>
            loadView("${g.createLink(controller: 'user', action: 'passwordForgotten')}", loadPasswordForgottenCallback);
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