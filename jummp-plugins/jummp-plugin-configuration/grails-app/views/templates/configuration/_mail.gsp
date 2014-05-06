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


<div class="dialog">
	<script type="text/javascript">
		$(document).ready(function () {
			$('#auth').click(function () {
				if (!$('#auth').is(':checked')) {
					$('#tlsRequired').attr("disabled", "disabled");
					$('#username').attr("disabled", "disabled");
					$('#password').attr("disabled", "disabled");
				}
				else {
					$('#tlsRequired').removeAttr("disabled");
					$('#username').removeAttr("disabled");
					$('#password').removeAttr("disabled");
				}
			});
		});
	</script>
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="host">Host:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'host', 'errors')}">
                    <input type="text" name="host" id="host" value="${mail?.host ?: '' }"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="port">Port:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'port', 'errors')}">
                    <input type="text" name="port" id="port" value="${mail?.port ?: '' }"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="auth">Authentication required:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'auth', 'errors')}">
                    <input type="checkbox" name="auth" id="auth" checked="checked" title="Authenticate on send"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="tlsRequired">TLS required:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'tlsRequired', 'errors')}">
                    <input type="checkbox" name="tlsRequired" id="tlsRequired"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="username">Username:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'username', 'errors')}">
                    <input type="text" name="username" id="username" value="${mail?.username ?: '' }"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="password">Password:</label></td>
                <td class="value ${hasErrors(bean: mail, field: 'password', 'errors')}">
                    <input type="password" name="password" id="password" value="${mail?.password ?: '' }"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
