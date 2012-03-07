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
    <content tag="sidebar">
        <div class="element">
            <h1>Gene Ontology Tree</h1>
            <h2>Browse models using GO Tree</h2>
            <p>This is a tree view of the models in this Database based on <a href="http://www.geneontology.org/">Gene Ontology</a>.</p>
            <p><g:link controller="gotree">link</g:link></p>
        </div>
    </content>
</html>
