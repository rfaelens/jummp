/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI), Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
*
* Additional permission under GNU Affero GPL version 3 section 7
*
* If you modify Jummp, or any covered work, by linking or combining it with [name of library] (or a modified version of that
* library), containing parts covered by the terms of [name of library's license], the licensors of this Program grant you additional
* permission to convey the resulting work. {Corresponding Source for a non-source form of such a combination shall include the source
* code for the parts of [name of library] used as well as that of the covered work.}
**/


package net.biomodels.jummp.plugins.configuration

import grails.validation.Validateable

/**
 * Command Object for validating subversion settings.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
@Validateable
class SvnCommand implements Serializable {
    private static final long serialVersionUID = 1L
    String localRepository

    static constraints = {
        localRepository(blank: false,
                        nullable: false,
                        validator: { repository, cmd ->
                            File directory = new File((String)repository)
                            // TODO: test whether the directory is an svn repository
                            return (directory.exists() && directory.isDirectory())
                        })
    }
}
