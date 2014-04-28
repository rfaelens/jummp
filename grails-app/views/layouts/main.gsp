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

<%
  		def contextHelpLocation=g.pageProperty(name:'page.contexthelp')
  		if (contextHelpLocation) {
  			contextHelpLocation=contextHelpLocation.trim()
  		}
  		else {
  			contextHelpLocation="manual"
  		}
  		int helpWidth=400;
%> 

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
    	var helpHidden=1;
    	<g:if test="${contextHelpLocation}">
    	var helpWidth=-1;
    	var maxWidth=-1;
    	var maxHelpWidth=800;
    	var minHelpWidth=50;
    	var stepWidth=30;
    	var syncResize=0;
    	function adjustWidth(newWidth) {
    		if (syncResize==1) {
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
	    	$("html, body").animate({ scrollTop: 0 }, "medium");
    		$( "#helpPanel" ).width(${helpWidth});
			$( "#helpPanel" ).show();
    		adjustWidth(${helpWidth});
    		$( "#helpPanel" ).position({
    			my: "right bottom",
    			at: "right bottom",
    			of: ".main-menu"
    		});
    		helpHidden=0;
    		$('#toggleHelp').text("Hide help");
    		$('#toggleHelp').attr("title", "Hide help");
    	}
    	
    	$(function() {
    		$( "#helpPanel" ).resizable({
    					handles: 'n,e,s,w',
    					maxWidth: maxHelpWidth,
    					animate: true,
    					resize: function( event, ui ) {
    						if (helpWidth==-1) {
    							helpWidth=ui.originalSize.width;
    						}
    						adjustWidth(ui.size.width);
    					}
    		});
    		$( "#helpPanel" ).draggable({ cursor: "move", revert: false});
    		$( "#helpPanel" ).hide();
		    $( "#expand" ).button({
		     	text: false,
		     	icons: {
		     		primary: "ui-icon-circle-plus"
		     	}
		    }).click(function() {
		    	 helpWidth=$("#helpPanel" ).width();
    			if (maxHelpWidth<helpWidth+stepWidth) {
    				helpWidth=maxHelpWidth;
    			}
    			else {
    				helpWidth+=stepWidth;
    			}
    			$( "#helpPanel" ).width(helpWidth);
		    });
		    $( "#contract" ).button({
		     	text: false,
		     	icons: {
		     		primary: "ui-icon-circle-minus"
		     	}
		    }).click(function() {
		    	 helpWidth=$("#helpPanel" ).width();
    			if (minHelpWidth>helpWidth-stepWidth) {
    				helpWidth=minHelpWidth
    			}
    			else {
    				helpWidth-=stepWidth;
    			}
    			$( "#helpPanel" ).width(helpWidth);
    			var addedPercentage = (helpWidth - 400) / 400
    			var basic = 99+addedPercentage;
    			$( "#helpFrame" ).width(basic+"%");
		    });
   		    $( "#close" ).button({
		     	text: false,
		     	icons: {
		     		primary: "ui-icon-circle-close"
		     	}
		    }).click(function() {
		    	hideHelp();
		    });

		    $( "#snap" ).button({
		    	text: false,
		    	icons: {
		     		primary: "ui-icon-arrowrefresh-1-e"
		    	}
		    }).click(function() {
		    	hideHelp();
		    	showHelp();
		    });
		    $( "#outlink" ).button({
		    	text: false,
		    	icons: {
		     		primary: "ui-icon-extlink"
		    	}
		    }).click(function() {
		    	hideHelp();
			    window.open('<ContextHelp:getURL location="${contextHelpLocation}"/>','_blank');
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
    	</g:if>
    </g:javascript>
    <g:javascript src="jummp.js"/>
    <g:javascript src="notification.js"/>
    <link rel="stylesheet" href="<g:resource dir="css" file="notification.css"/>" />
    <link rel="stylesheet" href="<g:resource dir="css" file="layout.css"/>" />
    
    <g:render template="/templates/${grailsApplication.config.jummp.branding.style}/head" />
    <g:layoutHead/>
</head>
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
	    		<button id="expand">Increase help size</button>
	    		<button id="contract">Decrease help size</button>
	    		<button id="snap">Reset help</button>
	    		<button id="outlink">Open in a new tab</button>
	    		<button id="close">Close</button>
	    	</div>
  			
  			<ContextHelp:getLink location="${contextHelpLocation}" width="${helpWidth}"/>
  		</div>
    </g:if>
    </div>
    </body>
</html>
