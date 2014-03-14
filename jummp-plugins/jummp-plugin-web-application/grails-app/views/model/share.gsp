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











<%@ page contentType="text/html;charset=UTF-8" %>
    <head>
        <meta name="layout" content="main" />
        <script src="http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js" type="text/javascript" charset="utf-8"></script>
        <script src="http://cdnjs.cloudflare.com/ajax/libs/handlebars.js/2.0.0-alpha.1/handlebars.min.js" type="text/javascript" charset="utf-8"></script>
        <script src="http://cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone-min.js" type="text/javascript" charset="utf-8"></script>
        <script id="collaborator-list-template" type="text/x-handlebars-template">
    			
        		<div id="currentCollabs">
				<h2>Collaborators</h2>
    			{{#if [].length}}
				<table class='table'> 
    			<thead>
    				<tr>
    					<td class="tableEL bold">Name</td>
    					<td class="tableEL  bold">Read</td>
    					<td class="tableEL bold">Write</td>
    			<tbody>
    		 		{{#each []}}
    		 			<tr class='collaborator'>
    		 				<td class="tableEL">{{this.name}}</td>
    		 				<td class="tableEL"><input id=checkRead-{{this.id}} data-field="read" data-person={{this.id}} class="updateCollab" type="checkbox" name="read" {{#if this.read}}checked=true{{/if}} {{#if this.disabledEdit}}disabled=true title="This user cannot be modified"{{/if}}></input></td>
    		 				<td class="tableEL"><input id=checkWrite-{{this.id}} data-field="write" data-person={{this.id}} class="updateCollab" type="checkbox" name="write" {{#if this.write}}checked=true{{/if}} {{#if this.disabledEdit}}disabled=true title="This user cannot be modified"{{/if}}></input></td>
    		 				<td class="tableEL"><button id=removebutton-{{this.id}} data-name={{this.name}} data-person={{this.id}} {{#if this.disabledEdit}}disabled=true title="This user cannot be modified"{{/if}} class="remove">Remove</button></td>
    		 			</tr>
    		 		{{/each}}
    		 	</tbody>
    		 	</table>
    			{{else}}
    		 		This model is not shared with anyone.
    		 	{{/if}}
    		 	</div>
    		 </script>
    		 <link rel="stylesheet" href="${resource(contextPath: "${grailsApplication.config.grails.serverURL}", dir: '/css', file: 'share.css')}" />
            		 
    </head>
    <body>
    		<div id="ui">
    			<div id="collabUI">
    				<div id="collabCreate">
    					<h2>Add New Collaborator</h2>
    					<form id="collaboratorAddForm">
    						<div id="formElements">
    							<div class="formElement">
    								<label for="name">Name</label>
    								<input id="nameSearch" name="name" type="text"/>
    							</div>
    							<div class="formElement">
									<label for="read">Read</label>
									<input type="checkbox" name="read"></input>
								</div>
								<div class="formElement">
									<label for="write">Write</label>
									<input type="checkbox" name="write"></input>
								</div>
							</div>
						<button id="AddButton">Add</button>
						<button id='SaveCollabs'>Save</button>
 		 	    	   </form>
					</div>
					<div class="containUI">
					</div>
					</div>
			    </div>
    		<g:javascript contextPath="" src="share.js"/>
    		<g:javascript>
    			$( document ).ready(function() {
    					main(JSON.parse('${permissions}'),
    						'<g:createLink controller="jummp" action="lookupUser"/>',
    						'<g:createLink controller="model" action="shareUpdate" id="${revision.model.id}"/>',
    						'<g:createLink controller="jummp" action="autoCompleteUser"/>',
    						'<g:createLink controller="model" action="show" id="${revision.model.id}"/>');
    			});
    		</g:javascript>
    </body>
    <content tag="title">
		Share model
	</content>
	<content tag="contexthelp">
		sharing
	</content>
    
