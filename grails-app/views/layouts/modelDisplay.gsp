<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), 
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the 
 terms of the GNU Affero General Public License as published by the Free 
 Software Foundation; either version 3 of the License, or (at your option) any 
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY 
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more 
 details.

 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.

 Additional permission under GNU Affero GPL version 3 section 7

 If you modify Jummp, or any covered work, by linking or combining it with 
 Apache Commons (or a modified version of that library), containing parts 
 covered by the terms of Apache License v2.0, the licensors of this 
 Program grant you additional permission to convey the resulting work. 
 {Corresponding Source for a non-source form of such a combination shall include 
 the source code for the parts of Apache Commons used as well as that of 
 the covered work.}
--%>










<g:applyLayout name="main">
<%@ page import="java.text.DateFormat"%>
<%
    def loadedZips=new HashMap();
    def zipSupported=[:]
%>
<head>
        <title>${revision.model.name}</title>
        <link rel="stylesheet" href="<g:resource dir="css/jqueryui/smoothness" file="jquery-ui-1.10.3.custom.css"/>" />

        <script type="text/x-mathjax-config">
        MathJax.Hub.Config({
            tex2jax: { inlineMath: [['$','$'],['\\(','\\)']] }
        });
    </script>
    <script type='text/javascript' src='http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML'></script>

        <g:javascript src="jquery/jquery-ui-v1.10.3.js"/>
        <g:javascript src="jstree/jquery.jstree.js"/>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'jstree.css')}" /> 
        <Ziphandler:outputFileInfoAsJS repFiles="${revision.files}" loadedZips="${loadedZips}" zipSupported="${zipSupported}"/>
        <script>
		$(function() {
			$( "#tabs" ).tabs({
				fx: { opacity: 'toggle' },
				select: function(event, ui) {
					jQuery(this).css('height', jQuery(this).height());
					jQuery(this).css('overflow', 'hidden');
				},
				show: function(event, ui) {
					jQuery(this).css('height', 'auto');
					jQuery(this).css('overflow', 'visible');
					}
			});
			$("#tabs ul li a").click(function (e) {
				var anchor=$(this).attr('href');
				if ($(this).attr('class')=="versionDownload") {
					openPage(anchor)
				}
				else if (typeof(anchor) != "undefined") {
					e.preventDefault();
					location.hash = anchor;
					window.scrollTo(0, 0);
				}
			});
			
		});
		
				
		function updateFileDetailsPanel(fileProps) {
			if (typeof(fileProps) != "undefined") {
				var content=[];
				content.push("<table cellpadding='2' cellspacing='5'>")
				for (var prop in fileProps) {
					if (prop!="isInternal" && fileProps[prop]) {
						content.push("<tr><td><b>",prop.replace("_"," "),"</b></td><td>",fileProps[prop])
						if (prop=="Name" && fileProps.isInternal==false) {
								content.push("<a title='Download ",fileProps[prop], "'","href='","${g.createLink(controller: 'model', action: 'downloadFile', id: revision.id)}");
								content.push("?filename=",encodeURIComponent(fileProps.Name),"'><img style='width:15px;margin-left:10px;float:none' src='http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Functional/Functional%20icons/download.png'/></a></div>");
						}
						content.push("</td></tr>");
						}
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
			$( "#download" ).button({
					text:false,
					icons: {
						primary:"ui-icon-arrowthickstop-1-s"
					}
			});
			$( "#update" ).button({
					text:false,
					icons: {
						primary:"ui-icon-refresh"
					}
			});
			$( "#publish" ).button({
					text:false,
					icons: {
						primary:"ui-icon-unlocked"
					}
			});
		});
	
	</script>
	<script>
        function openPage(loc) {
            window.location.href = loc;
        }
        function publishModel(url) {
            var shouldPublish = window.confirm("Make this version of the model visible to anyone without logging in?\nThis step is not reversible.");
            if (shouldPublish) {
                openPage(url);
            }
        }
    </script>
    <g:layoutHead/>
    <style>
        .spacedMargin {
            margin:10px;
        }
    </style>
    </head>
    <body>
    	<div id="topBar">
    		    <h2 style="float: left;">${revision.model.name}</h2>
    	        <div style="float:right;margin-top:10px">
                    <div id="modeltoolbar" style="display:inline"<%--class="ui-widget-header ui-corner-all"--%>>
                            <button id="download" onclick="return openPage('${g.createLink(controller: 'model', action: 'download', id: revision.id)}')">Download</button>
                            <g:if test="${canUpdate=="true"}">
                                <button id="update" onclick="return openPage('${g.createLink(controller: 'model', action: 'update', id: revision.model.id)}')">Update</button>
                            </g:if>
                            <g:if test="${showPublishOption=="true"}">
                                <button id="publish" onclick="return publishModel('${g.createLink(controller: 'model', action: 'publish', id: revision.id)}')">Publish</button>
                            </g:if>
                            <g:else>
                                <img style="float:right;margin-top:0;" title="This version of the model is public" alt="public model" src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Functional/Functional%20icons/unlock.png"/>
                            </g:else>
                     </div>
    	         </div>
   
    	        <%--        <a class="submit" title="Update Model" href="${g.createLink(controller: 'model', action: 'update', id: revision.model.id)}">Update</a> 	
        	<a class="submit" title="Download Model" href="${g.createLink(controller: 'model', action: 'download', id: revision.id)}">Download</a> 	
	 --%></div>
    	<div id="tablewrapper">
    	<div id="tabs">
	  <ul>
	    <li><a href="#Overview">Overview</a></li>
	    <li><a href="#Files">Files</a></li>
	    <li><a href="#History">History</a></li>
	    <g:pageProperty name="page.modelspecifictabs" />
	  </ul>
	  <div id="Overview">
	  	<label>Model Description:</label>
	  		<ul style="list-style-type: none;">
	  			<li>${revision.description}</li>
	  		</ul>
	    <table style="margin-top:30px">
		<tr>
		    <td><label><g:message code="model.model.format"/></label></td>
		    <td><div class='spaced'>${revision.format.name} ${revision.format.formatVersion?"(${revision.format.formatVersion})":""}</div></td>
		</tr>
		<tr>
		    <td><label><g:message code="model.model.publication"/>:</label></td>
		    <td>
		    	<%
		    		model=revision.model
		    	%>
		    	<div class='spaced'>
		    		<g:render  model="[model:model]" template="/templates/showPublication" />
		    	</div>
		    </td>
		<tr>
		</tr>
		</tr>
		<tr>
		    <td><label><g:message code="model.model.authors"/></label></td>
		    <td>
		    	<div class='spaced'>
		    		<g:each in="${authors}" status="i" var="author"><g:if test="${i>0}">, </g:if>${author}
		    		</g:each>
		    	</div>
		    </td>
		</tr>
	    </table>
	
	  </div>
	  <div id="Files">
	  	<div id="treeView">
	  		<ul>
	  		   <li rel="folder"><a>Main Files</a>
	  		   	<ul>
	  		   	   <Ziphandler:outputFileInfoAsHtml repFiles="${revision.files}" loadedZips="${loadedZips}" zipSupported="${zipSupported}" mainFile="${true}"/>
        	   	</ul>
	  		   </li>
	  		</ul>
	  		<ul>
	  		   <li><a>Additional Files</a>
   	  		   	<ul>
	  		   	   <Ziphandler:outputFileInfoAsHtml repFiles="${revision.files}" loadedZips="${loadedZips}" zipSupported="${zipSupported}" mainFile="${false}"/>
	  		   	</ul>
	  		   </li>
	  		</ul>
  		</div>
  		<div id="resizable">
  			<div id="detailsBox" class="detailsBox"></div>
  		</div>
  	  </div>
	  <div id="History">
	  	<% DateFormat dateFormat = DateFormat.getDateTimeInstance(); %>
	  	<ul>
	  		<li>Model owner: ${revision.model.submitter}</li>
	  		<li>Submitted: ${dateFormat.format(allRevs.first().uploadDate)}</li>
	  		<li>Last Modified: ${dateFormat.format(allRevs.last().uploadDate)}</li>
	  	</ul>
	  	<h5>Revisions</h5>
	  	<ul>
	  	     <g:each status="i" var="rv" in="${allRevs.sort{a,b -> a.revisionNumber > b.revisionNumber ? -1 : 1}}">
	  	     	<li style="${i==0 && allRevs.size()>1?"background-color:#FFFFCC;":""}margin-top:5px">
	  	     		Version: ${rv.revisionNumber} 
	  	     				<a class="versionDownload" title="download" href="${g.createLink(controller: 'model', action: 'download', id: rv.id)}">
	  	     					<img style="width:15px;float:none" src="http://www.ebi.ac.uk/web_guidelines/images/icons/EBI-Functional/Functional%20icons/download.png"/>
	  	     				</a>
	  	     			<ul>
	  	     				<li>Submitted on: ${dateFormat.format(rv.uploadDate)}</li>
	  	     				<li>Submitted by: ${rv.owner}</li>
	  	     				<li>With comment: ${rv.comment}</li>
	  	     			</ul>
	  	     	</li>
	  	     </g:each>
  		</ul>
	  </div>
	  <g:pageProperty name="page.modelspecifictabscontent" />
	</div>
    </div>

</body>
</g:applyLayout>
