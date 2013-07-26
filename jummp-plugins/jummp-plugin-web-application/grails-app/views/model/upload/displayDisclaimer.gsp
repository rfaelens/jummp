<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>
        	<g:if test="${isUpdate}">
        	      <g:message code="submission.disclaimer.update.title" args="${ [params.id] }" />
        	</g:if>
        	<g:else>
        	      <g:message code="submission.disclaimer.create.title"/>
        	</g:else>
        </title>
    </head>
    <body>
        <h1>Submission Guidelines</h1>
        <g:if test="${isUpdate}">
        	<g:message code="submission.disclaimer.updateMessage" args="${ [params.id] }" />
        </g:if>
        <g:else>
        	<g:message code="submission.disclaimer.createMessage"/>
        </g:else>
        <g:form>
            <div class="dialog">
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Continue" value="${g.message(code: 'submission.disclaimer.continueButton')}" />
                </div>
            </div>
        </g:form>
    </body>
</html>
