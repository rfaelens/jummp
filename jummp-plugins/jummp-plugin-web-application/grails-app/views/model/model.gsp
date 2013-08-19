<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>${revision.model.name}</title>
    </head>
    <body>
        <div>
	        <h2 style="float: left;">${revision.model.name}</h2>
        	<a href="${g.createLink(controller: 'model', action: 'download', id: revision.id)}" target="_blank"><r:img uri="/images/download.png" style="float: right; clear: right;"/></a> 	
        </div>
	<div>
	    <table>
		<tr>
		    <td class='key'><g:message code="model.model.version"/></td>
		    <td class='value'>${revision.revisionNumber}</td>
		    <td class='key'><g:message code="model.model.format"/></td>
		    <td class='value'>${revision.format.name}</td>
		</tr>
		<tr>
		    <td class='key'><g:message code="model.model.status"/></td>
		    <td class='value'>${revision.model.state}</td>
		    <td class='key'><g:message code="model.model.creationDate"/></td>
		    <td class='value'>${revision.model.submissionDate}</td>
		<tr>
		</tr>
		</tr>
	    </table>
	</div>
	<div>
	    <table>
		<tr>
		    <td class='key left'><g:message code="model.model.authors"/></td>
		    <td class='value'>
		    		<g:each in="${authors}" status="i" var="author"><g:if test="${i>0}">, </g:if>${author}
		    		</g:each>
		    </td>
		</tr>
	    </table>
	</div>
	<g:render template="${format}/showModel"/>
</body>
</html

