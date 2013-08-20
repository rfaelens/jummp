<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="layout" content="main"/>
        <title>${revision.model.name}</title>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
        <script src="http://www.ebi.ac.uk/~rali/js/jstree/jquery.jstree.js" type="text/javascript" ></script>
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
	#Files {
	    display: inline-block;
	    height: 100%;
	    width:100%;
	}
	#treeView {
	    float:left;
	    margin:5px;
	}
	#resizable { 
	    width: 70%; height: 150px; padding: 0.5em; border=0px; margin-left:5%;	  
	    float:left;
	 }
	</style>
	<script>
		var fileData=new Array();
		<g:each in="${revision.files}">
		 	<%
		 		File file=new File(it.path)
		 	%>
		 	fileData["${file.name}"]=new Object();
		 	fileData["${file.name}"].description="${it.description};"
		 	fileData["${file.name}"].mimeType="${it.mimeType};"
		 </g:each>
		 var abcd="abcd"
		 
		 
		$(function() {
			$( "#tabs" ).tabs()
			$("#tabs ul li a").click(function () {
				var anchor=$(this).attr('href');
				if (typeof(anchor) != "undefined") {
					location.hash = anchor;
				}
			});
		});
		
		function updateFileDetailsPanel(fileProps) {
			if (typeof(fileProps) != "undefined") {
				$("#Files #resizable #detailsBox").html(fileProps.mimeType);
			}
			else {
				$("#Files #resizable #detailsBox").html("");
			}
		}
		
		$(document).ready(function() {
			// Handler for .ready() called.
			$("#Files #treeView").bind("select_node.jstree", function(event, data) {
					var clickedOn=$(data.args[0]).text()
					clickedOn = clickedOn.replace(/^\s+|\s+$/g,'')
					var fileProps=fileData[clickedOn]
					updateFileDetailsPanel(fileProps)
				}).jstree({
				"ui" : {
					"select_limit" : 1
				},
				"themes" : {
					    "theme" : "classic",
					    "icons" : false
				},
				"plugins" : [ "themes", "html_data", "ui"]
			});
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
	  	<div id="treeView">
	  		<ul>
	  		   <li rel="folder"><a>Main Files</a>
	  		   	<ul>
	  		   	   <g:each in="${revision.files}">	
	  		   	   	<g:if test="${it.mainFile}">
	  		   	   		<li rel="file"><a>
	  		   	   			<%File f=new File(it.path);%>
	  		   	   			${f.name}
	  		   	   		</a></li>
	  		   	   	</g:if>
	  		   	   </g:each>
	  		   	</ul>
	  		   </li>
	  		</ul>
	  		<ul>
	  		   <li><a>Additional Files</a>
   	  		   	<ul>
	  		   	   <g:each in="${revision.files}">	
	  		   	   	<g:if test="${!it.mainFile}">
	  		   	   		<li rel="file"><a>
	  		   	   			<%File f=new File(it.path);%>
	  		   	   			${f.name}
	  		   	   		</a></li>
	  		   	   	</g:if>
	  		   	   </g:each>
	  		   	</ul>
	  		   </li>
	  		</ul>
  		</div>
  		<div id="resizable" class="ui-widget-content">
  			<div id="detailsBox" class="detailsBox"></div>
  		</div>
  	  </div>
	  <div id="History">
	  	<p>todo</p>
	  </div>
    	  <g:render template="${format}/modelSpecificTabImpl"/>
	</div>
        </div>

</body>
</html

