<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Submission Summary</title>
    </head>
    <body>
        <h1>Summary of Your Submission</h1>
        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="name">Name:</label>
                            </td>
                            <td class="value">
                                <g:textField readonly="readonly" name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label for="description">Description:</label>
                            </td>
                            <td class="value">
                                <g:textArea id="description" name="description" readonly="readonly" maxlength="5000" value='${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}'/>
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
                    <g:submitButton name="Continue" value="Continue" />
                    <g:submitButton name="Cancel" value="Abort" />
                </div>
            </div>
        </g:form>
    </body>
</html>
