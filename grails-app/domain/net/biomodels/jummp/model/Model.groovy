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





package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.ModelTransportCommand

/**
 * @short Representation of one Model.
 * This class is the representation of one Model. It contains the reference
 * to the model file stored in the version control system and the references
 * to the meta information such as publications and the list of revisions of
 * the Model.
 * The Model is the central domain class of Jummp.
 * @see Revision
 * @see Publication
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class Model implements Serializable {
    private static final long serialVersionUID = 1L
    /**
     * A Model has many Revision
     * IMPORTANT: never access revisions directly as this circumvents the ACL!
     * Use ModelService.getAllRevisions()
     */
    static hasMany = [revisions: Revision]
    /**
     * The path, relative to the folder containing all models,
     * of the folder dedicated to this model
     */
    String vcsIdentifier
    /**
     * The Publication the model has been described in
     */
    Publication publication
    /*
     * Whether the model has been deleted
    */
    boolean deleted = false
    /**
     * The perennial submission identifier of this model.
     */
    String submissionId
    /**
     * The perennial publication identifier of this model.
     */
    String publicationId
    // TODO: unique Identifier for the model? UML diagram lists an "accessionNumber"?

    static mapping = {
        publication lazy: false
        // publication_id is already taken
        publicationId column: 'perennialPublicationIdentifier'
    }

    def modelService
    static transients = ['modelService']

    static constraints = {
        vcsIdentifier(nullable: false, blank: false, unique: true)
        revisions(nullable: false, validator: { revs ->
            return !revs.isEmpty()
        })
        publication(nullable: true)
        deleted(nullable: false)
        submissionId blank: false
        publicationId nullable: true
    }

    ModelTransportCommand toCommandObject() {
        // TODO: is it correct to show the latest upload date as the lastModifiedDate or does it need ACL restrictions?
        Set<String> creators = []
        if (revisions) {
            revisions.each { revision ->
                creators.add(revision.owner.person.userRealName)
            }
        }
        def latestRev = modelService?.getLatestRevision(this);
        if (!latestRev) {
            latestRev = revisions? revisions.sort{ it.revisionNumber }.last() : null
        }
        return new ModelTransportCommand(
                id: id,
                submissionId: submissionId,
                publicationId: publicationId,
                name: latestRev ? latestRev.name : null,
                state: latestRev ? latestRev.state: null,
                lastModifiedDate: latestRev ? latestRev.uploadDate : null,
                format: latestRev ? latestRev.format.toCommandObject() : null,
                publication: publication ? publication.toCommandObject() : null,
                deleted: deleted,
                submitter: revisions ? revisions.sort{ it.revisionNumber }.first().owner.person.userRealName : null,
                submitterUsername: revisions ? revisions.sort{ it.revisionNumber }.first().owner.username : null,
                submissionDate: revisions ? revisions.sort{ it.revisionNumber }.first().uploadDate : null,
                creators: creators
        )
    }
}
