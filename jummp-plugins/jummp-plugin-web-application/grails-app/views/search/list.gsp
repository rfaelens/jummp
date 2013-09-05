<%@ page contentType="text/html;charset=UTF-8" %>
    <head>
        <title>Model Listing</title>
        <meta name="layout" content="main" />
        <g:javascript contextPath="" src="jquery/jquery.dataTables.js"/>
        <%-- 
         <link rel="stylesheet" href="${resource(contextPath: "/jummp", dir: '/css/jqueryui/south-street', file: 'jquery-ui-1.10.3.custom.css')}" />
        --%>
         <link rel="stylesheet" href="${resource(contextPath: "/jummp", dir: '/css', file: 'datatablestyle.css')}" />
         
         <g:javascript src="jquery/jquery-ui-v1.10.3.js"/>
        <g:javascript>
        	$(document).ready(function() {
        		 $.jummp.showModels.loadModelList();
        		 $.jummp.showModels.lastAccessedModels($("#sidebar-element-last-accessed-models"));
        	} );
        </g:javascript>
        <g:javascript contextPath="" src="showmodels.js"/>
    </head>
    <body activetab="search">
    	<h2><g:message code="model.list.heading"/></h2>
    	  <div class="content">
    	  <div class="view view-dom-id-9c00a92f557689f996511ded36a88594">
    	<div class="view-content">
        <table id="modelTable" class="views-table cols-4">
            <thead>
            <tr>
                <th><b><g:message code="model.list.name"/></b></th>
                <th><b><g:message code="model.list.format"/></b></th>
                <th><b><g:message code="model.list.submitter"/></b></th>
                <th><b><g:message code="model.list.submissionDate"/></b></th>
                <th><b><g:message code="model.list.modifiedDate"/></b></th>
            </tr>
            </thead>
            <tbody></tbody>
            <tfoot>
            <tr>
            </tr>
            </tfoot>
        </table>
        </div>
        </div>
        </div>
    </body>
    <content tag="sidebar">
        <div class="element" id="sidebar-element-last-accessed-models">
            <h2><g:message code="model.history.title"/></h2>
            <h3><g:message code="model.history.empty"/></h3>
            <p></p>
        </div>
        <%--  GoTree code, disabled until it is useful again.	
             <div class="element">  
            <h2>Gene Ontology Tree</h2>
            <h3>Browse models using GO Tree</h3>
            <p>This is a tree view of the models in this Database based on <a href="http://www.geneontology.org/">Gene Ontology</a>.</p>
            <p><g:link controller="gotree">link</g:link></p>
        </div> --%>
    </content>
    <content tag="browse">
    	selected
    </content>
