<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>${revision.model.name}</title>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
        <r:require module="jqueryui_latest"/>
        <style>
        #topBar {
            border-radius:6px;
	    padding: 8px;
	    float: top;
        }
        #tablewrapper {
            margin-top: 60px;
        }
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
	<script>
		$(function() {
			$( "#tabs" ).tabs()
			$("#tabs ul li a").click(function () {location.hash = $(this).attr('href');});
		});
	</script>
    </head>
    <body>
    	<div id="topBar">
    	        <h2 style="float: left;">${revision.model.name}</h2>
        	<a id="submit" href="${g.createLink(controller: 'model', action: 'download', id: revision.id)}">Download</a> 	
	</div>
    	<div id="tablewrapper">
	<div id="tabs">
	  <ul>
	    <li><a href="#Overview">Overview</a></li>
	    <li><a href="#Files">Files</a></li>
	    <li><a href="#History">History</a></li>
	    <g:render template="${format}/modelSpecificTabs"/>
	  </ul>
	  <div id="Overview">
	    ${revision.description}
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
		    <td class='key'><g:message code="model.model.formatversion"/></td>
		    <td class='value'>${revision.format.formatVersion}</td>
		<tr>
		</tr>
		</tr>
		<tr>
		    <td class='key left'><g:message code="model.model.authors"/></td>
		    <td class='value'>
		    		<g:each in="${authors}" status="i" var="author"><g:if test="${i>0}">, </g:if>${author}
		    		</g:each>
		    </td>
		    <td class='key'><g:message code="model.model.creationDate"/></td>
		    <td class='value'>${revision.model.submissionDate}</td>
		</tr>
	    </table>
	
	  </div>
	  <div id="Files">
	  	<table>
			  	<tr>
	  				<td><b>File Name</b></td>
	  				<td><b>Is Main?</b></td>
	  				<td><b>Mime Type</b></td>
	  				<td><b>Description</b></td>
	  			</tr>
	  			<g:each in="${revision.files}" var="item">
	  			<tr>
	  				<td>${(new File(item.path)).getName()}</td>
	  				<td>${item.mainFile}</td>
	  				<td>${item.mimeType}</td>
	  				<td>${item.description}</td>
	  			</tr>
	  			</g:each>
	  	</table>	
	  </div>
	  <div id="History">
	  	<p>todo</p>
	  </div>
    	  <g:render template="${format}/modelSpecificTabImpl"/>
	</div>
        </div>

</body>
</html

