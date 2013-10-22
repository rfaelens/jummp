<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>

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
                                    <g:textArea name="RevisionComments" rows="5" cols="70"/>
                                </td>
                          </tr>
                        </g:if>
                    </tbody>
                </table>
                <div class="buttons">
                    <g:submitButton name="Cancel" value="${g.message(code: 'submission.common.cancelButton')}" />
                    <g:submitButton name="Back" value="${g.message(code: 'submission.common.backButton')}" />
                    <g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
                    	<g:submitButton name="Continue" value="${g.message(code: 'submission.summary.update.submitButton')}"/>
                    </g:if>
                    <g:else>
                    	<g:submitButton name="Continue" value="${g.message(code: 'submission.summary.create.submitButton')}"/>
                    </g:else>
                </div>
            </div>
        </g:form>
    </body>
    <content tag="submit">
    	selected
    </content>
