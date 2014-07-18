<%--
 Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Create a team</title>
        <g:javascript src="underscore-min.js" plugin="jummp-plugin-web-application"/>
        <g:javascript src="handlebars.min.js" plugin="jummp-plugin-web-application"/>
        <g:javascript src="backbone-min.js" plugin="jummp-plugin-web-application"/>
        <script src="${resource(contextPath: "${grailsApplication.config.grails.serverURL}", dir: "/js", file: 'share.js')}"></script>
        <script id="team-member-template" type="text/x-handlebars-template">
            <td>{{this.name}}</td>
            <td><button type='button' id='remove-{{this.id}}' class='.remove'>Remove</button></td>
        </script>
        <script id="team-members-template" type="text/x-handlebars-template">
            {{#if isEmpty}}
                <p>Use the search box to add collaborators to this team.</p>
            {{else}}
                <table id="membersTable">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>&nbsp;</th>
                        </tr>
                    </thead>
                    <tbody>
                        {{#each}}
                            <tr>
                                <td>{{this.name}}</td>
                                <td><button id='remove-{{this.id}}' class='.remove'></button></td>
                            </tr>
                        {{/each}}
                    </tbody>
                </table>
            {{/if}
        </script>
        <g:javascript src="teams.js" plugin="jummp-plugin-security"/>
   </head>
    <body>
        <g:form name="newTeamForm" action="save">
            <table class='spaced'>
                <tbody>
                    <tr>
                        <td><label class="required" for="name">Name</label></td>
                        <td><span><g:textField required="true" autofocus="true" maxlength="255" name="name"/></span></td>
                    </tr>
                    <tr>
                        <td><label for="description">Description</label></td>
                        <td><span><g:textField maxlength="255" name="description"/></span></td>
                    </tr>
                </tbody>
            </table>
            <div id="membersContainer" class='spaced'>
                <h2>Team Members</h2>
                <div>
                    <label for="nameSearch">User</label>
                    <input placeholder="Name, username or email" id="nameSearch" name="nameSearch" type="text"/>
                </div>
                <span class="tip">
                    <span class='tipNote'>Tip:</span>
                    Choose the collaborator, then press enter to add them to this team.
                </span>
                <div class="spaced" id="members">
                    <table id="membersTable">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>&nbsp;</th>
                            </tr>
                        </thead>
                        <tbody id="membersTableBody">
                            <!-- ADD MEMBERS HERE. -->
                        </tbody>
                    </table>
                </div>
            </div>

            <g:submitButton name="create" value="Create"/>
        </g:form>
        <g:javascript>
            $(function() {
                var url = '<g:createLink controller="jummp" action="autoCompleteUser"/>';
                // collaborators are declared in share.js
                autoComplete([], url);
            });
        </g:javascript>
    </body>
</html>
<content tag="title">Create a team</content>
