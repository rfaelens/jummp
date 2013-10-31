<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



<div class="dialog">
    <table class="formtable">
        <tbody>
            <tr class="prop">
                <td class="name"><label for="startRemoveOffset" title="Offset before trigger initially runs">Start remove offset:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'startRemoveOffset', 'errors')}">
                    <input type="text" name="startRemoveOffset" id="startRemoveOffset" value="${trigger?.startRemoveOffset ? trigger.startRemoveOffset : "300000"}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name"><label for="removeInterval" title="Remove interval of the trigger">Remove interval:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'removeInterval', 'errors')}">
                    <input type="text" name="removeInterval" id="removeInterval" value="${trigger?.removeInterval ? trigger?.removeInterval : "1800000"}"/>
                </td>
            </tr>
        <tr class="prop">
                <td class="name"><label for="maxInactiveTime" title="The maximum inactive interval of users">Maximum inactive time:</label></td>
                <td class="value ${hasErrors(bean: trigger, field: 'maxInactiveTime', 'errors')}">
                    <input type="text" name="maxInactiveTime" id="maxInactiveTime" value="${trigger?.maxInactiveTime ? trigger?.maxInactiveTime : "1800000"}"/>
                </td>
            </tr>
        </tbody>
    </table>
</div>
