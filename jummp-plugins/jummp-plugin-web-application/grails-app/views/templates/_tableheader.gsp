<th>
	<a href="${createLink(controller: 'search', action: action, 
			params: [query: query,  sortDir: sortDirection=="asc"? "desc":"asc", sortBy: sortColumn])}">
	<b><g:message code="${msgCode}"/></b>
		<g:if test="${sortBy==sortColumn && sortDirection=="asc"}">
			<g:img dir="${grailsApplication.config.grails.serverURL}/images" 
			contextPath="" file="ascend.gif"
			alt="Sort Descending"/>
		</g:if>
		<g:if test="${sortBy==sortColumn && sortDirection=="desc"}">
			<g:img dir="${grailsApplication.config.grails.serverURL}/images" 
			contextPath="" file="descend.gif"
			alt="Sort Ascending"/>
		</g:if>
	</a>
</th>
