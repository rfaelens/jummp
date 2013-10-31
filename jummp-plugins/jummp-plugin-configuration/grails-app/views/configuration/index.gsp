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
        <title>Configuration</title>
    </head>
    <body>
        <div id="remote" class="body">
            <h1>Configuration</h1>
            <p>Select the module to configure:
                <ul>
                    <li><a href="${createLink(action: 'database')}">Database</a></li>
                    <li><a href="${createLink(action: 'ldap')}">LDAP</a></li>
                    <li><a href="${createLink(action: 'svn')}">Subversion</a></li>
                    <li><a href="${createLink(action: 'vcs')}">Version Control System</a></li>
                    <li><a href="${createLink(action: 'remote')}">Remote</a></li>
                    <li><a href="${createLink(action: 'server')}">Server</a></li>
                    <li><a href="${createLink(action: 'bives')}">Model Versioning System - BiVeS</a></li>
                    <li><a href="${createLink(action: 'userRegistration')}">User Registration</a></li>
                    <li><a href="${createLink(action: 'changePassword')}">Change/Reset Password</a></li>
                    <li><a href="${createLink(action: 'branding')}">Select Branding</a></li>
                    <li><a href="${createLink(action: 'cms')}">Content Management System</a></li>
                </ul>
            </p>
        </div>
    </body>
    <g:render template="/templates/configuration/configurationSidebar"/>
</html>
