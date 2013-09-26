<%@ page contentType="text/html;charset=UTF-8" %>
    <head>
        <title>Model Search</title>
        <meta name="layout" content="main" />
        <g:javascript contextPath="" src="jquery/jquery.dataTables.js"/>
        <%-- 
         <link rel="stylesheet" href="${resource(contextPath: "/jummp", dir: '/css/jqueryui/south-street', file: 'jquery-ui-1.10.3.custom.css')}" />
        --%>
         <link rel="stylesheet" href="${resource(contextPath: "/jummp", dir: '/css', file: 'datatablestyle.css')}" />
         
         <g:javascript contextPath="" src="jquery/jquery-ui-v1.10.3.js"/>
        <g:javascript>
        	$(document).ready(function() {
        		 $.jummp.showModels.searchModels('${query}');
        	} );
        </g:javascript>
        <g:javascript contextPath="" src="showmodels.js"/>
    </head>
    <body activetab="search">
    	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/searchBox"/>
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
    <content tag="searchQuery">
    		${query}
    </content>
    
