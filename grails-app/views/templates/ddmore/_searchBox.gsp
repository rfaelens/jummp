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










<div class="mini-search">
<form action="${createLink(controller: 'search', action: 'searchRedir')}" method="post" id="search-block-form" accept-charset="UTF-8"><div><div class="container-inline">
                    	<h2 class="element-invisible">Search form</h2>
                    	<div class="form-item form-type-textfield form-item-search-block-form">
                    	<label class="element-invisible" for="edit-search-block-form--2">Search </label>
                    		<input title="Enter the terms you wish to search for." value="${query}" type="text" id="edit-search-block-form--2" name="search_block_form" value="" size="15" maxlength="128" class="form-text" />
                    	</div>
                    	<div class="form-actions form-wrapper" id="edit-actions"><input type="submit" id="edit-submit--2" name="op" value="Search" class="form-submit" /></div><input type="hidden" name="form_build_id" value="form-jQbJYvHUYLoH48sWG5A4Ymf-JtV_Wc7QgFb80D8xPi8" />
                    	<input type="hidden" name="form_id" value="search_block_form" />
                    	</div>
</div></form></div>
