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
import net.biomodels.jummp.core.model.RevisionTransportCommand
import net.biomodels.jummp.core.model.RepositoryFileTransportCommand as RFTC
import net.biomodels.jummp.core.model.ModelState


/**
 * @short A Revision represents one version of a Model.
 * The Revision is stored in the Version Control System (VCS) and is linked
 * to the VCS through a list of Revisions. The Revision is required to retrieve any file from
 * the VCS and to store new files.
 * A Revision is linked to one Model and each Model has several Revision, but
 * at least one.
 * @see Model
 *
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class Revision implements Serializable {
    private static final long serialVersionUID = 1L
    
    def modelService
    
    /**
     * The revision belongs to one Model
     */
    static belongsTo = [model: Model]
    static hasMany = [repoFiles: RepositoryFile]
    static mapping = {
        description type: 'text'
    }
    /**
     * The revision number in the version control system.
     * E.g. in Subversion the global revision number of the
     * commit which stored this Model Revision or in git the
     * Sha1-sum of the commit which stored this Model Revision.
     */
    String vcsId

    /**
     * Revision number in reference to the Model and not to the VCS.
     */
    Integer revisionNumber
    /**
     * The user who uploaded the Revision.
     */
    User owner
    /**
     * Whether the revision is a minor change or not.
     */
    Boolean minorRevision
    
    /**
     * Whether the revision has been validated
     */
    Boolean validated = false
    /**
     * The name of the 'model'. Stored in revision as it may change. Annoying.
     */
    String name
    
    /**
     * The description of the 'model'. Stored in revision as it may change
     */
    String description
    
    /**
     * The "commit message" of this revision.
     */
    String comment
    /**
     * The date when the Revision was uploaded.
     */
    Date uploadDate
    /**
     * The model the revision belongs to
     */
    Model model
    /**
     * The format of the file in the VCS.
     * Kept in the Revision and not in the Model to make it possible to upload a new Revision in a different format.
     */
    ModelFormat format
    /**
     * The state of the Model, by default UNPUBLISHED
     */
    ModelState state = ModelState.UNPUBLISHED
    /**
     * Indicates whether this Revision is marked as deleted.
     */
    Boolean deleted = false
    // TODO: UML diagram lists a "format" and a "state". Do these belong here? What is the type of them?

    static constraints = {
        model(nullable: false)
        vcsId(nullable: false,  unique: 'model')
        revisionNumber(nullable: false, unique: 'model')
        owner(nullable: false)
        minorRevision(nullable: false)
        validated(nullable: false)
        uploadDate(nullable: false)
        name(nullable:true)
        description(nullable: true)
        comment(nullable: true, maxSize: 1000)
        format(nullable: false)
        deleted(nullable: false)
        state(nullable: false)
    }
    
    
    List<RFTC> getRepositoryFilesForRevision() {
        List<RFTC> repFiles=new LinkedList<RFTC>()
        List<File> files=modelService.retrieveModelRepFiles(this)
        repoFiles.each { rf ->
            File tmpFile=files.find { it.getName() == (new File(rf.path)).getName() }
            RFTC rftc=new RFTC(
            	id: rf.id,
                path: tmpFile.getCanonicalPath(),
                description: rf.description,
                hidden: rf.hidden,
                mainFile: rf.mainFile,
                userSubmitted: rf.userSubmitted,
                mimeType: rf.mimeType)
            repFiles.add(rftc)
        }
        return repFiles
    }

    RevisionTransportCommand toCommandObject() {
        RevisionTransportCommand rev=new RevisionTransportCommand(
                id: id,
                state:state,
                revisionNumber: revisionNumber,
                owner: owner.person.userRealName,
                minorRevision: minorRevision,
                validated: validated,
                name: name,
                description: description,
                comment: comment,
                uploadDate: uploadDate,
                format: format.toCommandObject(),
                model: model.toCommandObject(),
        )
        return rev
    }
}
