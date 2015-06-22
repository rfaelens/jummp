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
 */

package net.biomodels.jummp.core

import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import net.biomodels.jummp.annotationstore.*
import net.biomodels.jummp.model.Revision
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.perf4j.aop.Profiled

/**
 * Service class that facilitates interaction with model metadata.
 *
 * The service currently provides means of querying the metadata related to a particular
 * revision of a model.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
@CompileStatic
class MetadataService {
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_DEBUG_ENABLED = log.isDebugEnabled()

    /**
     * Fetches any ResourceReferences defined for a given qualifier from a revision.
     *
     * Convenience method for fetching statements that match qualifier @p qName in revision
     * @p $rev.
     *
     * @param rev the identifier of an existing revision.
     * @param qName the accession of the desired qualifier.
     * @return a list of ResourceReferences that represent the objects of the statements from
     * the revision denoted by @param rev.
     * @see net.biomodels.jummp.annotationstore.ResourceReference
     * @see net.biomodels.jummp.annotationstore.Statement
     */
    @CompileDynamic
    @Profiled(tag = "metadataService.findAllResourceReferencesForQualifier")
    List<ResourceReference> findAllResourceReferencesForQualifier(Long rev, String qName) {
        if (IS_DEBUG_ENABLED) {
            log.debug "Finding annotations with qualifier $qName for revision $rev."
        }
        Revision revision = Revision.load(rev)
        List<ResourceReference> result = ResourceReference.executeQuery('''
                select resourceReference
                from ElementAnnotation elementAnnotation
                    join elementAnnotation.statement statement
                    join statement.object resourceReference
                    join statement.qualifier qualifier
                where
                    elementAnnotation.revision = ? and
                    qualifier.accession = ?''', [revision, qName])
        if (IS_DEBUG_ENABLED) {
            log.debug "Found ${result.size()} matching annotations."
        }
        return result
    }

    /**
     * Fetches ResourceReferences associated with the supplied qualifiers.
     *
     * @param revision the identifier of an existing revision.
     * @param qualifiers a list of qualifiers for which ResourceReferences should be found.
     * @return a list where the supplied qualifiers  the keys and the corresponding
     * values are lists of ResourceReferences.
     */
    @CompileStatic
    @Profiled(tag = "metadataService.findAllResourceReferencesForQualifiers")
    List<ResourceReference> findAllResourceReferencesForQualifiers(Long revision,
            List<String> qualifiers) {
        return qualifiers.collect { String q ->
            findAllResourceReferencesForQualifier(revision, q)
        }
    }
}
