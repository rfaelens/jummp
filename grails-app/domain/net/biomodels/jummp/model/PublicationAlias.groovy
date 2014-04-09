/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
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
**/





package net.biomodels.jummp.model
import net.biomodels.jummp.plugins.security.Alias
/**
 * @short Join table for Publication and Alias with a field for specifying author 
 * position.
 * @see Publication, Alias
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 * @author Mihai Glont <mglont@ebi.ac.uk>
 */
class PublicationAlias implements Serializable {
	private static final long serialVersionUID = 1L
    Publication publication
	Alias alias
	Integer authorPosition
	
	static mapping = {
        id composite: ['publication', 'alias']
    }
}
