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
**/





package net.biomodels.jummp.core.model

/**
 * @short Wrapper for a Model to be transported through JMS.
 *
 * Small wrapper class to decouple the Model from the Database.
 * Changes to instances of this class are not populated to the database.
 * The object does not contain references to the Revisions. Use the
 * service methods to retrieve Revisions of this Model.
 *
 * The object can also be used as a command object for the web interface.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Raza Ali <raza.ali@ebi.ac.uk>
 */
class ModelTransportCommand implements Serializable {
    private static final long serialVersionUID = 1L
    Long id
    String name
    boolean deleted
    /**
     * Only needed to upload new Models. When retrieving Models set to format of latest revision.
     */
    ModelFormatTransportCommand format = null
    /**
     * Only needed to upload new Models. When retrieving Models it is unset.
     */
    String comment = null
    /**
     * The date of the latest revision the user has access to.
     */
    Date lastModifiedDate
    /**
     * Information about the Publication.
     */
    PublicationTransportCommand publication
    /**
     * The original submitter of the first revision.
     */
    String submitter
    /**
     * The date when the Model was uploaded first to the instance.
     */
    Date submissionDate
    /**
     * The names of all users who have worked on this Model.
     */
    Set<String> creators
    ModelState state
    String submitterUsername
}
