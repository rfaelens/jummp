<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Model Listing</title>
        <meta name="layout" content="main" />
        <r:require module="showModels"/>
        <jqDT:resources/>
    </head>
    <body activetab="search">
        <table id="modelTable">
            <thead>
            <tr>
                <th><g:message code="model.list.modelId"/></th>
                <th><g:message code="model.list.name"/></th>
                <th><g:message code="model.list.publicationId"/></th>
            </tr>
            </thead>
            <tbody></tbody>
            <tfoot>
            <tr>
                <th><g:message code="model.list.modelId"/></th>
                <th><g:message code="model.list.name"/></th>
                <th><g:message code="model.list.publicationId"/></th>
            </tr>
            </tfoot>
        </table>

    <r:script>
$(function() {
    $.jummp.showModels.loadModelList();
    $.jummp.showModels.lastAccessedModels($("#sidebar-element-last-accessed-models"));
});
    </r:script>
    </body>
    <content tag="sidebar">
        <div class="element" id="sidebar-element-last-accessed-models">
            <h1><g:message code="model.history.title"/></h1>
            <h2><g:message code="model.history.empty"/></h2>
            <p></p>
        </div>
        <div class="element">
            <h1>Gene Ontology Tree</h1>
            <h2>Browse models using GO Tree</h2>
            <p>This is a tree view of the models in this Database based on <a href="http://www.geneontology.org/">Gene Ontology</a>.</p>
            <p><g:link controller="gotree">link</g:link></p>
        </div>
    </content>
</html>
