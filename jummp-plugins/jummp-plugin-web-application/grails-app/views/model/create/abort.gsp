<%@ page contentType="text/html;charset=UTF-8" %>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.abort.common.title"/></title>
    </head>
    <body>
        <h2><g:message code="submission.abort.create.header"/></h2>
        <p><g:message code="submission.abort.create.message" args="${[createLink(controller: 'model', action: 'create')]}"/></p>
    </body>
    <content tag="submit">
    	selected
    </content>
