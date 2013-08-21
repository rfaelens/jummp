<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title><g:message code="submission.summary.header"/></title>
    </head>
    <body>
    	<h2><g:message code="submission.summary.header"/></h2>
        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="name">
                                    <g:message code="submission.summary.nameLabel"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                          	<label name="name">${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="description">
                                    <g:message code="submission.summary.descriptionLabel"/>
                                </label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                            	<div class="displayDescription">
                                	<label name="description">${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}</label>
                                </div>
                            </td>
                        </tr>
                        <g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
                            <tr class="prop">
                                <td class="name">
                                    <label for="RevisionComments">
                                    	<g:message code="submission.summary.revisionLabel"/>
                                    </label>
                                </td>
                                <td class="value">
                                    <g:textField name="RevisionComments" maxlength="50"/>
                                </td>
                          </tr>
                        </g:if>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:submitButton name="Continue" value="${g.message(code: 'submission.summary.submitButton')}"/>
                </div>
            </div>
        </g:form>
    </body>
</html>