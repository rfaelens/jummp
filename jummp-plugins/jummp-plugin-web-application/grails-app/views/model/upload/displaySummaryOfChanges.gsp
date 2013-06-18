<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Summary Of Changes</title>
  </head>
  <body>
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"> <label for="name">Name:</label></td>
                <td class="value"><label name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/></td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="description">Description:</label></td>
                <td class="value"><label name="description" maxlength="5000" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}"/></td>
            </tr>
            <tr>
            <g:if test="${workingMemory.get("isUpdateOnExistingModel") as Boolean}">
              <tr class="prop">
                  <td class="name"> <label for="RevisionComments">Revision Comments:</label></td>
                  <td class="value"><g:textField name="RevisionComments" maxlength="50"/></td>
              </tr>
            </g:if>
 
              <td><g:submitButton name="Continue" value="Continue" /></td>
              <td><g:submitButton name="Cancel" value="Abort" /></tr>

        </tbody>
    </table>
  </body>
</html>
