/**
* Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with
* Apache Commons (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Commons used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.webapp


class ShareTagLib {
	static namespace="share"
	 
	
	def outputTemplates = { attrs ->
        String output=''' <script id="collaborators_tpl" type="text/underscore" charset="utf-8">

    </script>
    <script id="collaborator_tpl" type="text/underscore" charset="utf-8">
      <form data-id="<%= collaborator.id %>" class="collaborator-form large-9 columns">
        <div class="large-5 columns">
          <label for="collaborator">Collaborator</label>
          <input type="text" name="name">
          </input>
        </div>
        <div class="large-1 columns without-label">
          <button class="small button">Save</button>
        </div>
      </form>
    </script>''';
		out<<output
	}
}
