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





package net.biomodels.jummp.core.model
import net.biomodels.jummp.core.model.audit.*
/**
 * @short Wrapper for ModelAudit to be transported.
 *
 * Small wrapper class to decouple the ModelAudit from the Database.
 * Changes to instances of this class are not populated to the database.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class ModelAuditTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long id
    ModelTransportCommand model 
    /**
     * Who accessed the model
     */
    String username
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

}
