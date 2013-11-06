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











<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="changePassword">Users can change their password:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'changePassword', 'errors')}">
                    <input type="checkbox" name="changePassword" id="changePassword" ${!changePassword || changePassword.changePassword ? 'checked="checked"' : ''} title="Users can change their password"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="resetPassword">Users can request a reset password mail:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'resetPassword', 'errors')}">
                    <input type="checkbox" name="resetPassword" id="resetPassword" ${!changePassword || changePassword.resetPassword ? 'checked="checked"' : ''} title="Users can request a reset password mail"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="senderAddress">Sender Address:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'senderAddress', 'errors')}">
                    <input type="text" name="senderAddress" id="senderAddress" value="${changePassword?.senderAddress ?: 'user@example.org'}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="subject">Subject:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'subject', 'errors')}">
                    <input type="text" name="subject" id="subject" value="${changePassword?.subject ?: 'Example Password Change'}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="body">Body:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'body', 'errors')}">
                    <textarea id="body" rows="20" cols="40" name="body">${changePassword?.body ?: 'Example Text: Get a new password.'}</textarea>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="url">URL for Reset Password:</label></td>
                <td class="value ${hasErrors(bean: changePassword, field: 'url', 'errors')}">
                    <input type="text" name="url" id="url" value= "${changePassword?.url ?: 'http://example.org:8080/jummp/'}"/><span>user/resetPassword/{{CODE}}</span>
                </td>
            </tr>
        </tbody>
    </table>
</div>
