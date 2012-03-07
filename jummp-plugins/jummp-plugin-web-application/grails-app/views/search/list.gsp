<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Model Listing</title>
        <meta name="layout" content="main" />
        <r:require module="showModels"/>
        <jqDT:resources/>
    </head>
    <body activetab="search">
        <div id="modelNavigation">
            <span id="modelNavigationOffset" style="display: none">${offset ? offset : "0"}</span>
            <span id="modelNavigationSorting" style="display: none">${sort ? sort : "0"}</span>
            <span id="modelNavigationDirection" style="display: none">${dir ? dir : "asc"}</span>
        </div>
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

    <r:script>
$(function() {
    $.jummp.showModels.loadModelList();
    $.jummp.showModels.loadModelTabs();
});
    </r:script>
    </body>

</html>
