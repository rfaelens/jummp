<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
 Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the
 terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 
 You should have received a copy of the GNU Affero General Public License along 
 with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>





<!doctype html>
<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/precursor" />
<html>
<head>
	<g:javascript library="jquery" plugin="jquery"/>
    <g:javascript src="jquery/jquery.i18n.properties-min-1.0.9.js"/>
    <g:javascript src="jquery/jquery-ui-v1.10.3.js"/>
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
    <style>
    	#mainframe {
    		position: relative;
    		min-width:1000px;
    	}
    	#helpbutton { 
    			 height: 104px; 
    			 width: 104px; 
    			 position: fixed; 
    			 top: 40%; 
    			 z-index: 999;
    			 transform: rotate(-90deg);
    			 -webkit-transform: rotate(-90deg); 
    			 -moz-transform: rotate(-90deg); 
    			 -o-transform: rotate(-90deg); 
    			 filter: progid:DXImageTransform.Microsoft.BasicImage(rotation=3);
    	}
    	#resizable {
    		height:100%;
    	}
    	#helpbutton a { 
    		display: block; 
    		background: gray; 
    		height: 15px; 
    		width: 70px; 
    		padding: 8px 16px;
    		text-align: center;
    		color: #fff; 
    		font-family: Arial, sans-serif; 
    		font-size: 17px; 
    		font-weight: bold; 
    		text-decoration: none; 
    		border-bottom: solid 1px #333;
    		border-left: solid 1px #333;
    		border-right: solid 1px #fff;
    	}

    	#helpbutton a:hover { 
    		background: #06c; 
    	}
    	
    	#helpFrame {
           width: 100%; 
           height: 100%; 
           overflow: scroll;
        }
    	
    </style>
    <g:javascript>
    	$.appName = "${grailsApplication.metadata["app.name"]}";
    	$.serverUrl = "${grailsApplication.config.grails.serverURL}";
    	$.i18n.properties({
    		name: 'messages',
    		path: "${grailsApplication.config.grails.serverURL}/js/i18n/",
    		mode: "map"
    	});
    	var helpWidth=-1;
    	var helpHidden=1;
    	
    	function adjustWidth(newWidth) {
    		var delta= helpWidth - newWidth;
    		$( "#mainframe" ).width($( "#mainframe" ).width() + delta);
    		helpWidth=newWidth;
    	}
    	
    	$(function() {
    		$( "#resizable" ).resizable({
    					handles: 'w',
    					maxWidth: 400,
    					resize: function( event, ui ) {
    						if (helpWidth==-1) {
    							helpWidth=ui.originalSize.width;
    						}
    						adjustWidth(ui.size.width);
    					}
    		});
		    $( "#resizable" ).hide();

		    $('#toggleHelp').click(function(event) {
    			event.preventDefault();
    			if (helpHidden==1) {
    				helpWidth=-1;
    				$( "#resizable" ).width(280);
					$( "#resizable" ).show();
    				adjustWidth(300);
    				$( "#resizable" ).position({
    					my: "left top",
    					at: "right top+10%",
    					of: "#mainframe"
    				});
    				helpHidden=0;
    				$('#toggleHelp').text("Hide");
    				$('#toggleHelp').attr("title", "Hide help");
    			}
    			else {
    				adjustWidth(0);
    				$( "#resizable" ).hide();
    				helpHidden=1;
    				$('#toggleHelp').text("Help");
    				$('#toggleHelp').attr("title", "Access help for this page");
    			}
    		});
    	
		    
    	});
    	
    </g:javascript>
    <g:javascript src="jummp.js"/>
    <g:javascript src="notification.js"/>
    <link rel="stylesheet" href="<g:resource dir="css" file="notification.css"/>" />
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/head" />
    <g:layoutHead/>
</head>
    <%
  		def contextHelpLocation=g.pageProperty(name:'page.contexthelp')
  		if (contextHelpLocation) {
  			contextHelpLocation=contextHelpLocation.trim()
  		}
  	%> 
  	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/bodyTag"/>
	<div id="totality">
  	<div id="mainframe">
    	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/header"/>
    	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/mainbody"/>
    	<g:render template="/templates/${grailsApplication.config.jummp.branding.style}/footer"/>
    </div>
  	<g:if test="${contextHelpLocation}">
    	<div id="helpbutton">
    		<a id="toggleHelp" title="Access help for this page" href="#">Help</a>
    	</div>
	    <div id="resizable">
  			<ContextHelp:getLink location="${contextHelpLocation}"/>
  		</div>
    </g:if>
    </div>
    </body>
</html>
