<g:applyLayout name="main">
<%@ page import="java.nio.file.Files"%>
<%@ page import="java.nio.file.attribute.BasicFileAttributes"%>
<%@ page import="java.nio.file.FileSystem"%>
<%@ page import="java.nio.file.FileSystems"%>
<%@ page import="java.nio.file.Path"%>
<%@ page import="java.nio.file.Paths"%>
<%@ page import="java.nio.file.SimpleFileVisitor"%>
<%@ page import="java.nio.file.FileVisitResult"%>
<%@ page import="org.apache.commons.io.FilenameUtils"%>
<%@ page import="java.text.DateFormat"%>
<%
	def loadedZips=new HashMap();
	def zipSupported=[:]
%>
<head>
        <title>${revision.model.name}</title>
        <link rel="stylesheet" href="<g:resource dir="css/jqueryui/smoothness" file="jquery-ui-1.10.3.custom.css"/>" />
 
        <g:javascript src="jquery/jquery-ui-v1.10.3.js"/>
        <g:javascript src="jstree/jquery.jstree.js"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
        <script>
		var fileData=new Array();
		<g:each in="${revision.files}">
			<%
	 		File file=new File(it.path)
	 		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class)
	 		%>
	 		fileData["${file.name}"]=new Object();
	 		fileData["${file.name}"].Name="${FilenameUtils.getName(it.path)}";
	 		fileData["${file.name}"].Extension="${FilenameUtils.getExtension(it.path)}";
	 		<g:if test="${!it.mainFile}">
	 			fileData["${file.name}"].Description="${it.description}";
	 		</g:if>
	 		fileData["${file.name}"].Type="${it.mimeType}";
	 		fileData["${file.name}"].Size=readablizeBytes(${attr.size()});
	 		fileData["${file.name}"].Created="${new Date(attr.creationTime().toMillis())}";
	 		fileData["${file.name}"].Accessed="${new Date(attr.lastAccessTime().toMillis())}";
	 		fileData["${file.name}"].Modified="${new Date(attr.lastModifiedTime().toMillis())}";

	 		<g:if test="${it.mimeType.contains('zip')}">
	 			<%
	 			     Path zipfile = Paths.get(it.path);
	 			     final URI uri = URI.create("jar:file:" + zipfile.toUri().getPath());
	 			     final Map<String, String> env = new HashMap<>();
	 			     try {
  	 			     FileSystem fs = FileSystems.newFileSystem(uri, env);
  	 			     loadedZips.put(uri, fs)
  	 			     final Path root = fs.getPath("/");
	 			     Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
	 				@Override
	 				public FileVisitResult visitFile(Path visiting, BasicFileAttributes attrs) throws IOException {
	 					out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"]=new Object();'<<'\n';
	 					out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"].Name="'<<FilenameUtils.getName(visiting.toString())<<'";'<<'\n';
	 					out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"].Extension="'<<FilenameUtils.getExtension(visiting.toString())<<'";'<<'\n';
	 					out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"].Size=readablizeBytes('<<attrs.size()<<');'<<'\n';
	 					if (attrs.lastAccessTime()) {
	 						out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"].Accessed="'<<new Date(attrs.lastAccessTime().toMillis())<<'";'<<'\n';
	 					}
	 					if (attrs.lastModifiedTime()) {
	 						out<<'fileData[\"'<<zipfile.getFileName().toString()<<visiting.toString()<<'\"].Modified="'<<new Date(attrs.lastModifiedTime().toMillis())<<'";'<<'\n';
	 					}
	 					return FileVisitResult.CONTINUE;
	 				}
	 			     });
		 			     zipSupported[it.path]=true;
	 			     }
	 			     catch(Exception unsupportedZip) { 
	 			     	     zipSupported[it.path]=false;
	 			     }
	 			%>
	 		
	 		</g:if>
	 	</g:each>
		 
		 
		 
		$(function() {
			$( "#tabs" ).tabs()
			$("#tabs ul li a").click(function () {
				var anchor=$(this).attr('href');
				if (typeof(anchor) != "undefined") {
					location.hash = anchor;
				}
			});
		});
		
		
		function readablizeBytes(bytes) {
			var s = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
			var e = Math.floor(Math.log(bytes) / Math.log(1024));
			return (bytes / Math.pow(1024, e)).toFixed(2) + " " + s[e]; 
		}
		
		function updateFileDetailsPanel(fileProps) {
			if (typeof(fileProps) != "undefined") {
				var content=[];
				content.push("<table>")
				for (var prop in fileProps) {
					content.push("<tr><td><b>",prop,"</b></td><td>",fileProps[prop],"</td></tr>");
				}
				content.push("</table>");
				$("#Files #resizable #detailsBox").html(content.join(""));
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
			var tree = $("#Files #treeView");
			tree.bind("loaded.jstree", function (event, data) {
				tree.jstree("open_all");
			});
		});
	
	</script>
	<g:layoutHead/>
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
	    <g:pageProperty name="page.modelspecifictabs" />
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
	  		   	   			${f.name}</a>
	  		   	   			<g:if test="${it.mimeType.contains('zip') && zipSupported[it.path]}">
  		   	   				  <ul>
 	  		   	   			  <%
	  		   	   				Path zipfile = Paths.get(it.path);
	  		   	   				final URI uri = URI.create("jar:file:" + zipfile.toUri().getPath());
	  		   	   				final Map<String, String> env = new HashMap<>();
	  		   	   				//FileSystem fs = FileSystems.newFileSystem(uri, env);
	  		   	   				FileSystem fs=loadedZips.get(uri);
	  		   	   				final Path root = fs.getPath("/");
	  		   	   				Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
	  		   	   					@Override
	  		   	   					public FileVisitResult visitFile(Path visiting, BasicFileAttributes attrs) throws IOException {
	  		   	   						out<<'<li><a>'<<zipfile.getFileName().toString()<<visiting.toString()<<'</a></li>';
	  		   	   						return FileVisitResult.CONTINUE;
	  		   	   					}
	  		   	   				});
	  		   	   			  %>
	  		   	   			  </ul>
	  		   	   			</g:if>
	  		   	   		</li>
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
	  		   	   			${f.name}</a>
	  		   	   			<g:if test="${it.mimeType.contains('zip') && zipSupported[it.path]}">
  		   	   				  <ul>
 	  		   	   			  <%
	  		   	   				Path zipfile = Paths.get(it.path);
	  		   	   				final URI uri = URI.create("jar:file:" + zipfile.toUri().getPath());
	  		   	   				final Map<String, String> env = new HashMap<>();
	  		   	   				//FileSystem fs = FileSystems.newFileSystem(uri, env);
	  		   	   				FileSystem fs=loadedZips.get(uri);
	  		   	   				final Path root = fs.getPath("/");
	  		   	   				Files.walkFileTree(root, new SimpleFileVisitor<Path>(){
	  		   	   					@Override
	  		   	   					public FileVisitResult visitFile(Path visiting, BasicFileAttributes attrs) throws IOException {
	  		   	   						out<<'<li><a>'<<zipfile.getFileName().toString()<<visiting.toString()<<'</a></li>';
	  		   	   						return FileVisitResult.CONTINUE;
	  		   	   					}
	  		   	   				});
	  		   	   			  %>
	  		   	   			  </ul>
	  		   	   			</g:if>
	  		   	   		</li>
	  		   	   	</g:if>
	  		   	   </g:each>
	  		   	</ul>
	  		   </li>
	  		</ul>
  		</div>
  		<div id="resizable">
  			<div id="detailsBox" class="detailsBox"></div>
  		</div>
  	  </div>
	  <div id="History">
	  	<h5>Model owned by ${revision.model.submitter}</h5>
	  	<% DateFormat dateFormat = DateFormat.getDateTimeInstance(); %>
	  	<ul>
	  		<li>Submitted: ${dateFormat.format(allRevs.first().uploadDate)}</li>
	  		<li>Last Modified: ${dateFormat.format(allRevs.last().uploadDate)}</li>
	  	</ul>
	  	<h5>Revisions</h5>
	  	<ul>
	  	     <g:each in="${allRevs}">
	  	     	<li>Submitted by ${it.owner} on ${dateFormat.format(it.uploadDate)}, with comment: ${it.comment}</li>  
	  	     </g:each>
  		</ul>
	  </div>
	  <g:pageProperty name="page.modelspecifictabscontent" />
	</div>
        </div>

</body>
</g:applyLayout>
