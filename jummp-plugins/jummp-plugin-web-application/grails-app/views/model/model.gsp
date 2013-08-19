<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>${revision.model.name}</title>
        <style>
        	#submit {
	    background-color: #5CAD85;
	    -moz-border-radius: 5px;
	    -webkit-border-radius: 5px;
	    border-radius:6px;
	    margin-top:3px;
	    margin-right:8px;
	    color: #fff;
	    font-family: 'Verdana';
	    font-size: 20px;
	    text-decoration: none;
	    cursor: pointer;
	    padding: 8px;
	     border:none;
	     float: right;
	}

	#submit:hover {
	    border: none;
	    background:#339966;
	    box-shadow: 0px 0px 1px #777;
	
	}
    </style>
    </head>
    <body>
        <div>
	        <h2 style="float: left;">${revision.model.name}</h2>
        	<a id="submit" href="${g.createLink(controller: 'model', action: 'download', id: revision.id)}">Download</a> 	
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

