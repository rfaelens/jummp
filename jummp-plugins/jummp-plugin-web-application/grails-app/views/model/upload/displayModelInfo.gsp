<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="net.biomodels.jummp.core.model.RevisionTransportCommand" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Display Model Info</title>
  </head>
  <body>
    <g:form>
      
      <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"> <label for="name">Name:</label></td>
                <td class="value"><g:textField name="name" maxlength="50" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).name}"/></td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="description">Description:</label></td>
                <td class="value"><g:textField name="description" maxlength="5000" value="${(workingMemory.get("RevisionTC") as RevisionTransportCommand).description}"/></td>
            </tr>
            <tr>
              <td><g:submitButton name="Continue" value="Continue" /></td>
              <td><g:submitButton name="Cancel" value="Abort" /></tr>

        </tbody>
    </table>
    </g:form>
  </body>
</html>
