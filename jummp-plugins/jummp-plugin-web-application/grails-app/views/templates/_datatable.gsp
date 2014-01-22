<%
	def totalCount
	String action="list"
	if (matches) {
		totalCount=matches
		action="search"
	}
	else {
		totalCount=modelsAvailable
	}
	def imagePath="/images"
%>
<div class="content">
   	<div class="view view-dom-id-9c00a92f557689f996511ded36a88594">
    	<div class="view-content">
    		<g:if test="${models}">
    	    	<table id="modelTable" class="views-table cols-4">
        	    <thead>
                <tr>
                	<g:render template="/templates/tableheader" model="[action: action, 'sortColumn': 'name','msgCode':'model.list.name']"/>
                	<g:render template="/templates/tableheader" model="[action: action, 'sortColumn': 'format','msgCode':'model.list.format']"/>
                	<g:render template="/templates/tableheader" model="[action: action, 'sortColumn': 'submitter','msgCode':'model.list.submitter']"/>
                	<g:render template="/templates/tableheader" model="[action: action, 'sortColumn': 'submitted','msgCode':'model.list.submissionDate']"/>
                	<g:render template="/templates/tableheader" model="[action: action, 'sortColumn': 'modified','msgCode':'model.list.modifiedDate']"/>
                </tr>
                </thead>
                <tbody>
                	<g:each status="i" in="${models}" var="model">
                		<tr class="${ (i % 2) == 0 ? 'even' : 'odd'}">
                			<td>
                				<a href="${createLink(controller: 'model', id: model.id, action: 'show')}">
                					${model.name}
                				</a>
                			</td>
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
                	<%
						int currentPage=1
						if (offset!=0) {
							currentPage = Math.ceil((double) (offset+1) / (double) length)
						}
						int modelStart=1 + (currentPage - 1)*length
						int modelEnd= length < models.size() ? length : models.size()
						modelEnd+=modelStart -1
						int numPages= Math.ceil((double) totalCount / (double) length)
                	%>
                <div class="dataTables_info">
                	Showing ${modelStart} to ${modelEnd} of ${totalCount} models
                </div>
                <div class="dataTables_paginate">
                	<g:if test="${currentPage==1}">
                		<g:img dir="${imagePath}/pagination" absolute="true" contextPath="" file="arrow-previous-disable.gif" alt="Previous"/>
                	</g:if>
                	<g:else>
                		<a href="${createLink(controller: 'search', action: action, params: [query: query, sortDir: sortDirection, sortBy: sortBy, offset: modelStart-length-1])}">
                			<g:img dir="${imagePath}/pagination" absolute="true"  contextPath="" file="arrow-previous.gif" alt="Previous"/>
                		</a>
                	</g:else>
                	<g:each var="i" in="${ (1..<numPages+1) }">
                		<span class="pageNumbers">
                			<g:if test="${currentPage==i}">
                				${i}
                			</g:if>
                			<g:else>
                				<a href="${createLink(controller: 'search', action: action, params: [query: query,  sortDir: sortDirection, sortBy: sortBy, offset: (i - 1)*length ])}">
                					${i}
                				</a>
                			</g:else>
                		</span>
                	</g:each>
                	<g:if test="${modelEnd==totalCount}">
                		<g:img dir="${imagePath}/pagination" absolute="true"  contextPath="" file="arrow-next-disable.gif" alt="Next"/>
                	</g:if>
                	<g:else>
                		<a href="${createLink(controller: 'search', action: action, params: [query: query,  sortDir: sortDirection, sortBy: sortBy, offset: modelStart+length-1])}">
                			<g:img dir="${imagePath}/pagination" absolute="true"  contextPath="" file="arrow-next.gif" alt="Next"/>
                		</a>
                	</g:else>
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
