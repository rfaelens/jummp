<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <title>Model Listing</title>
        <meta name="layout" content="main" />
        <r:require modules="showModels,jqueryui_latest"/>
        <jqDT:resources/>
        <g:render template="/templates/search/tablestyles"/>
    </head>
    <body activetab="search">
        <table id="modelTable">
            <thead>
            <tr>
                <th><b><g:message code="model.list.modelId"/></b></th>
                <th><b><g:message code="model.list.name"/></b></th>
                <th><b><g:message code="model.list.publicationId"/></b></th>
            </tr>
            </thead>
            <tbody></tbody>
            <tfoot>
            <tr>
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
            <h2><g:message code="model.history.title"/></h2>
            <h3><g:message code="model.history.empty"/></h3>
            <p></p>
        </div>
        <div class="element">
            <h2>Gene Ontology Tree</h2>
            <h3>Browse models using GO Tree</h3>
            <p>This is a tree view of the models in this Database based on <a href="http://www.geneontology.org/">Gene Ontology</a>.</p>
            <p><g:link controller="gotree">link</g:link></p>
        </div>
    </content>
</html>
