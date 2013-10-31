<%--
 Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)

 This file is part of Jummp.

 Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.

 Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
--%>



<div id="modeSwitch">
        <!-- TODO: active class has to be set on really selected mode -->
        <a href="${g.createLink(controller: 'search', action: 'list')}"><jummp:button class="left"><g:message code="jummp.main.search"/></jummp:button></a>
        <a href="${g.createLink(controller: 'model', action: 'create')}"><jummp:button class="right"><g:message code="jummp.main.submit"/></jummp:button></a>
</div>
    
