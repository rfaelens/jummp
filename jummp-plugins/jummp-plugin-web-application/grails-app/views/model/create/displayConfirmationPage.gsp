<%@ page contentType="text/html;charset=UTF-8" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.confirmation.common.title"/></title>
    </head>
    <body>
        <h2><g:message code="submission.confirmation.create.header"/></h2>
        <p><g:message code="submission.confirmation.create.first.message"/></p>
        <p><g:message code="submission.confirmation.create.second.message" args="${[createLink(action:"show", id:session.result_submission)]}"/></p>        
    </body>
    <content tag="submit">
    	selected
    </content>
