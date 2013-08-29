<%@ page contentType="text/html;charset=UTF-8" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.abort.common.title"/></title>
    </head>
    <body>
        <h2><g:message code="submission.abort.update.header"/></h2>
        <p><g:message code="submission.abort.update.message" args="${[session.result_submission, g.createLink(controller: 'model', action: 'update', id:session.result_submission)]}"/></p>
    </body>
    <content tag="submit">
    	selected
    </content>
