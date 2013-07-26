<%@ page contentType="text/html;charset=UTF-8" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Submit Model</title>
    </head>
    <body>
        <h1>Submission Guidelines</h1>
        <g:if test="${isUpdate}">
        	<p>This will be modifying model ${params.id}. Are you sure?</p>
        </g:if>
        <g:else>
        	<p>This will create a new model. Are you sure?</p>
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
