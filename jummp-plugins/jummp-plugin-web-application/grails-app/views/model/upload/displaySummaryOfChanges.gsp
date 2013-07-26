<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Submission Summary</title>
    </head>
    <body>
    	<h1><g:message code="submission.summary.header"/></h1>
        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="name">Name:</label>
                            </td>
                            <td class="value" style="vertical-align:top;">
                       <%--          <g:textField readonly="readonly" name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/>
                        --%>
                        	     <label name="name">${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name" style="vertical-align:top;">
                                <label for="description">Description:</label>
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
                                    <label for="RevisionComments">Summary of the changes</label>
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
