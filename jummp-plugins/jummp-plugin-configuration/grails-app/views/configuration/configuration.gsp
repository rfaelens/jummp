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











<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Configuration - ${title}</title>
    </head>
    <body>
        <g:hasErrors>
            <div class="errors">
                <g:renderErrors/>
            </div>
        </g:hasErrors>
        <div id="remote" class="body">
            <h1>Configuration - ${title}</h1>
            <g:form action="${action}">
                <g:render template="/templates/configuration/${template}"/>
                <div class="buttons">
                    <jummp:button id="cancelButton">Cancel</jummp:button>
                    <jummp:button id="submitButton">Save</jummp:button>
                </div>
            </g:form>
        </div>
        <g:javascript>
$("#cancelButton").click(function() {
    $("form")[0].reset();
});
$("#submitButton").click(function() {
    $("form")[0].submit();
});
        </g:javascript>
    </body>
    <g:render template="/templates/configuration/configurationSidebar"/>
</html>
