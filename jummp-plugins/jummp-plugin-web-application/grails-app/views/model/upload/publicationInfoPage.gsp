<%@ page contentType="text/html;charset=UTF-8" %>
    <head>
        <meta name="layout" content="main"/>
        <title>
        	<g:if test="${isUpdate}">
        	      <g:message code="submission.publicationInfoPage.update.title" args="${ [params.id] }" />
        	</g:if>
        	<g:else>
        	      <g:message code="submission.publicationInfoPage.create.title"/>
        	</g:else>
        </title>
    </head>
    <body>
        <h2>Update Publication Information</h2>
        <g:if test="${isUpdate}">
        	<g:message code="submission.publicationInfoPage.updateMessage" args="${ [params.id] }" />
        </g:if>
        <g:else>
        	<g:message code="submission.publicationInfoPage.createMessage"/>
        </g:else>
        <g:form>
            <div class="dialog">
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:submitButton name="Continue" value="${g.message(code: 'submission.publication.continueButton')}" />
                </div>
            </div>
        </g:form>
    </body>
    <content tag="submit">
    	selected
    </content>
