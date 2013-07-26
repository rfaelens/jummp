<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.confirmation.common.title"/></title>
    </head>
    <body>
        <h1><g:message code="submission.confirmation.update.header"/></h1>
        <p><g:message code="submission.confirmation.update.first.message"/></p>
        <p><g:message code="submission.confirmation.update.second.message" args="${[createLink(action:"model", id:session.result_submission)]}"/></p>        
   </body>
</html>
