<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <title>Setup</title>
    </head>
    <body>
        <g:hasErrors bean="${sbml}">
            <div class="errors">
                <g:renderErrors bean="${sbml}"/>
            </div>
        </g:hasErrors>
        <div id="sbml" class="body">
            <h1>SBML Settings</h1>
            <g:form name="firstRun" action="setup">
                <g:render template="/templates/configuration/sbml"/>
                <div class="buttons">
                    <g:submitButton name="back" value="Back"/>
                    <g:submitButton name="next" value="Next"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
