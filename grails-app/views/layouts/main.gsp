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
    	var maxWidth=-1;
    	var maxHelpWidth=600;
    	var draggable=0;
    	
    	function adjustWidth(newWidth) {
    		if (draggable==0) {
    			var delta= helpWidth - newWidth;
    			var mainWidth=$("#mainframe" ).width();
    			if (maxWidth==-1) {
    				maxWidth=mainWidth;
    			}
    			if (maxWidth<mainWidth+delta) {
    				mainWidth=maxWidth;
    			}
    			else {
    				mainWidth+=delta;
    			}
    			$( "#mainframe" ).width(mainWidth);
    			helpWidth=newWidth;
    		}
    	}
    	
    	function hideHelp() {
	    	adjustWidth(0);
    		$( "#helpPanel" ).hide();
    		helpHidden=1;
    		helpWidth=-1;
    		$('#toggleHelp').text("Help");
    		$('#toggleHelp').attr("title", "Access help for this page");
    	}
    	
    	function showHelp() {
	    	helpWidth=-1;
    		$( "#helpPanel" ).width(250);
			$( "#helpPanel" ).show();
    		adjustWidth(250);
    		$( "#helpPanel" ).position({
    			my: "left top",
    			at: "right top+8%",
    			of: "#mainframe"
    		});
    		helpHidden=0;
    		$('#toggleHelp').text("Hide");
    		$('#toggleHelp').attr("title", "Hide help");
    	}
    	
    	$(function() {
    		$( "#helpPanel" ).resizable({
    					handles: 'w',
    					maxWidth: maxHelpWidth,
    					resize: function( event, ui ) {
    						if (helpWidth==-1) {
    							helpWidth=ui.originalSize.width;
    						}
    						adjustWidth(ui.size.width);
    					}
    		});
    		$( "#helpPanel" ).draggable({ cursor: "move", revert: true});
    		$( "#helpPanel" ).hide();
		    $( "#expand" ).button({
		     	text: false,
		     	icons: {
		     		primary: "ui-icon-arrow-4"
		     	}
		    }).click(function() {
		    	if (draggable!=1) {
		    		$( "#helpPanel" ).draggable( "option", "revert", false );
		    		draggable=1;
		    	}
		    	else {
		    		$( "#helpPanel" ).draggable( "option", "revert", true );
		    		draggable=0;
		    	}
		    });
		    
		    $( "#snap" ).button({
		    	text: false,
		    	icons: {
		     		primary: "ui-icon-pin-s"
		    	}
		    }).click(function() {
		    	hideHelp();
		    	showHelp();
		    	draggable=0;
		    });
		    
		    $('#toggleHelp').click(function(event) {
    			event.preventDefault();
    			if (helpHidden==1) {
    				showHelp();
    			}
    			else {
    				hideHelp();
    			}
    		});
    	
		    
    	});
    </g:javascript>
    <g:javascript src="jummp.js"/>
    <g:javascript src="notification.js"/>
    <link rel="stylesheet" href="<g:resource dir="css" file="notification.css"/>" />
    <link rel="stylesheet" href="<g:resource dir="css" file="layout.css"/>" />
    
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
	    <div id="helpPanel">
	    	<div id="toolbar" class="ui-widget-header ui-corner-all">
	    		<input type="checkbox" value="false" id="expand"><label for="expand">Drag</label>
	    		<button id="snap">Snap to page</button>
	    	</div>
  			
  			<ContextHelp:getLink location="${contextHelpLocation}"/>
  		</div>
    </g:if>
    </div>
    </body>
</html>
