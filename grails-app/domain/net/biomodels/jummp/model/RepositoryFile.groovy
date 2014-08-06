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
* Apache Tika (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Apache Tika used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.RepositoryFileTransportCommand
import org.apache.tika.detect.DefaultDetector
import org.apache.tika.metadata.Metadata

/**
 * @short Representation of a File belonging to a ModelVersion.
 *
 * @see net.biomodels.jummp.model.Revision
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class RepositoryFile implements Serializable {
    static belongsTo = [revision:Revision]
    /**
     * Dependency Injection of FileSystemService
     */
    def fileSystemService
    /**
     * The path to the file associated with this file. Although in the database we
     * only store the name of the file, we know the unique vcsIdentifier of the 
     * model folder that contains it. When an instance of this class is wrapped in a 
     * corresponding command object, we work out the absolute location of the file.
     */
    String path
    /**
     * A brief description of the purpose of the file in relation to the model
     */
    String description
    /**
     * Flag to differentiate between the files that should be displayed to the user and those that should not.
     * By default, they are the former.
     */
    boolean hidden = false
    /**
     * Flag for distinguishing the main entry of a submission from the rest. This will be considered the main
     * model file. For instance, in the case of DDMoRe submissions this should be the PharmML file. If set to true,
     * this must be in XML or ZIP format.
     */
    boolean mainFile = false
    /**
     * Set to true if this file was provided by the user during submission, rather than automatically generated.
     */
    boolean userSubmitted = false
    /**
     * The content type of this file as defined in http://www.iana.org/assignments/media-types/
     */
    String mimeType

    static constraints = {
        path(blank: false, nullable: false,
            validator: { p, rf ->
                if (!p || !new File(p).exists()) {
                    return false
                }
                def f = new File(p).getCanonicalFile()
                def sherlock = new DefaultDetector()
                String properType = sherlock.detect(new BufferedInputStream(
                        new FileInputStream(f)), new Metadata()).toString()
                if (!rf.mimeType.equals(properType)) {
                    rf.mimeType = properType
                }
                return true
            })
        description(nullable: true, maxSize:500)
        //content type detection is performed above, when we validate the path of the file
        mimeType(nullable: true)
    }
}
