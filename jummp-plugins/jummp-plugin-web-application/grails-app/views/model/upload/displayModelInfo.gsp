<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>Model Information</title>
    </head>
    <body>
        <h1>Model Information</h1>
        <p>Please ensure the following fields are correctly filled in.</p>

        <g:form>
            <div class="dialog">
                <table class="formtable">
                    <tbody>
                        <tr class="prop">
                            <td class="name">
                                <label for="name">Name:</label>
                            </td>
                            <td class="value">
                                <g:textField name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td class="name">
                                <label for="description">Description:</label>
                            </td>
                            <td class="value">
                                <g:textField name="description" maxlength="5000" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}"/>
                            </td>
                        </tr>
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
