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
                <td class="name"><label for="brandingInternalColor">Internal
                        Color:</label></td>
                <td
                    class="value ${hasErrors(bean: branding, field: 'internalColor', 'errors')}">
                    <input type="text" name="internalColor"
                    id="brandingInternalColor"
                    value="${branding ? branding?.internalColor : ''}" />
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="brandingExternalColor">External
                        Color:</label></td>
                <td
                    class="value ${hasErrors(bean: branding, field: 'externalColor', 'errors')}">
                    <input type="text" name="externalColor"
                    id="brandingExternalColor"
                    value="${branding ? branding?.externalColor : ''}" />
                </td>
            </tr>
        </tbody>
    </table>
</div>
========================
<br />
${grailsApplication.config.jummp.branding.internalColor}
<br />
========================
