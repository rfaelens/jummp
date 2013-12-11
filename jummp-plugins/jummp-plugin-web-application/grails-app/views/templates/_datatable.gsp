<%
	def totalCount
	if (matches) {
		totalCount=matches
	}
	else {
		totalCount=modelsAvailable
	}
%>
<div class="content">
   	<div class="view view-dom-id-9c00a92f557689f996511ded36a88594">
    	<div class="view-content">
    		<g:if test="${models}">
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
                <tbody>
                	<g:each status="i" in="${models}" var="model">
                		<tr class="${ (i % 2) == 0 ? 'even' : 'odd'}">
                			<td>${model.name}</td>
                			<td>${model.format.name}</td>
                			<td>${model.submitter}</td>
                			<td>${model.submissionDate.format('yyyy/MM/dd')}</td>
                			<td>${model.lastModifiedDate.format('yyyy/MM/dd')}</td>
                		</tr>
                	</g:each>
                </tbody>
                <tfoot>
                	<tr>
                	</tr>
                </tfoot>
                </table>
                <div class="dataTables_info">
                	Showing 1 of ${models.size()} of ${totalCount} models.
                </div>
                <div class="dataTables_paginate">
                	Pagination Controls go here
                </div>
            </g:if>
            <g:else>
            	<g:if test="${matches!=null}">
            		No available models matched your query. Please try logging in to access more models, or another search query.
            	</g:if>
            </g:else>
        </div>
    </div>
</div>
