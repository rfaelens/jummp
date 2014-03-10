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

import net.biomodels.jummp.plugins.security.User
import net.biomodels.jummp.core.model.audit.AccessFormat
import net.biomodels.jummp.core.model.audit.AccessType

/**
 * @short Domain class for storing audit info about a Model.
 * 
 * An audit item consists of a Model, the user who accessed it, the means of
 * access, the method used to access it, information about the changes made and
 * when it was accessed.
 * 
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class ModelAudit implements Serializable {
    static mapping = {
        changesMade type: "text"
    }
    /**
     * Which model was accessed
     */
    Model model
    /**
     * Who accessed the model - can be null if anonymous
     */
    User user
    /**
     * How the model was accessed
     */
    AccessFormat format
    /**
     * Why the model was accessed
     */
    AccessType type
    /**
     * What changes were made
     */
     String changesMade
    /**
     * When the model was accessed
     */
    Date dateCreated
    /**
     * Whether the model access succeeded
     */
    boolean success

    static constraints = {
        model(nullable: false)
        user(nullable: true)
        format(nullable: false)
        type(nullable: false)
        changesMade(nullable: true)
        success defaultValue: false, nullable: false;
    }

}
