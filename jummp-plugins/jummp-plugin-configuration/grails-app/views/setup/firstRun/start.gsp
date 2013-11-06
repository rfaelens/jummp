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
        <title>First Run Wizard</title>
    </head>
    <body>
        <div id="remote" class="body yui-skin-sam">
            <h1>Create Admin Account</h1>
            <g:form name="adminForm" action="firstRun">
                <div class="dialog">
                    <table class="formtable">
                        <tbody>
                            <tr class='prop'>
                                <td  class='name'><label>Login Name:</label></td>
                                <td  class='value'><input type="text" name='username' /></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Full Name:</label></td>
                                <td  class='value'><input type="text" name='userRealName'/></td>
                            </tr>

                        %{--TODO hide in LDAP setup--}%
                            <tr class='prop'>
                                <td  class='name'><label>Password:</label></td>
                                <td  class='value'><input type="password" name='password'/></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Confirm Password:</label></td>
                                <td  class='value'><input type="password" name='rePassword'/></td>
                            </tr>

                            <tr class='prop'>
                                <td  class='name'><label>Email:</label></td>
                                <td  class='value'><input type="text" name='email'/></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <g:submitButton name="next" value="Create"/>
                </div>
            </g:form>
        </div>
    </body>
</html>
