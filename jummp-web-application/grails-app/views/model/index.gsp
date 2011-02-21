<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta name="layout" content="main"/>
        <jqDT:resources jqueryUi="true"/>
        <title><g:message code="model.list.title"/></title>
    </head>
    <body>
        <div id="body">
            <table id="modelTable">
                <thead>
                   <tr>
                      <th><g:message code="model.list.modelId"/></th>
                      <th><g:message code="model.list.name"/></th>
                      <th><g:message code="model.list.publicationId"/></th>
                      <th><g:message code="model.list.lastModificationDate"/></th>
                      <th><g:message code="model.list.format"/></th>
                   </tr>
                </thead>
                <tbody></tbody>
                <tfoot>
                   <tr>
                      <th><g:message code="model.list.modelId"/></th>
                      <th><g:message code="model.list.name"/></th>
                      <th><g:message code="model.list.publicationId"/></th>
                      <th><g:message code="model.list.lastModificationDate"/></th>
                      <th><g:message code="model.list.format"/></th>
                   </tr>
                </tfoot>
            </table>
        </div>
    </body>
</html>
