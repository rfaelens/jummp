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
* Spring Security (or a modified version of that library), containing parts
* covered by the terms of Apache License v2.0, the licensors of this
* Program grant you additional permission to convey the resulting work.
* {Corresponding Source for a non-source form of such a combination shall
* include the source code for the parts of Spring Security used as well as
* that of the covered work.}
**/





package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.model.Revision
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.model.Model

/**
 * @short Thread to resolve all MIRIAM annotations used in a Revision.
 *
 * This class is configured as a Spring Bean of scope prototype, that is each
 * time an instance of this class is required a new instance is created by the
 * Spring container.
 *
 * As Spring is used to configure the dependencies it is not allowed to construct
 * an instance directly. To retrieve an instance of this class use:
 * @code
 * grailsApplication.mainContext.getBean("fetchAnnotations", revision)
 * @endcode
 *
 * The Thread operates on one Revision and takes care of setting the Authentication in
 * the ThreadLocal SecurityContext to the one used in the Thread which requested an
 * instance of this class. In order to run the thread queue the Thread e.g. in a
 * Thread Pool.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class FetchAnnotationsThread implements Runnable {
    /**
     * Dependency Injection of MIRIAM Service
     */
    def miriamService
    /**
     * Dependency Injection of ModelFileFormatService
     */
    def modelFileFormatService
    /**
     *
     */
    def modelService

    /**
     * The Revision this thread will operate on
     */
    private Long model
    /**
     * The Authentication of the user who uploaded the Revision
     */
    private Authentication authentication
    /**
     * The revision on which we operate
     */
    private Revision revision
    /**
     * Optional Id of the revision to fetch
     */
    private Long revisionId

    void run() {
        // set the Authentication in the Thread's SecurityContext
        SecurityContextHolder.context.setAuthentication(authentication)
        // perform the operation
        Model threadModel = Model.get(model)
        if (!threadModel) {
            // when started from a different Hibernate session it is possible that
            // the transaction is not yet written to the database and the session
            // bound to this Thread cannot yet access the Model. By waiting a small
            // amount of time it becomes likely that we can access the Model in this thread.
            try {
                Thread.sleep(10000)
            } catch (InterruptedException e) {
                // ignore
            }
            threadModel = Model.get(model)
        }
        if (!revisionId) {
            revision = modelService.getLatestRevision(threadModel)
        } else {
            revision = Revision.get(revisionId)
            if (!revision) {
                try {
                    Thread.sleep(10000)
                } catch (InterruptedException e) {
                    // ignore
                }
                revision = Revision.get(revisionId)
            }
        }
        if (revision) {
            modelFileFormatService.getAllAnnotationURNs(revision).each {
                miriamService.queueUrnForIdentifierResolving(it, revision)
            }
        }
        // clear the Authentication from the Thread's SecurityContext
        SecurityContextHolder.clearContext()
    }

    static public FetchAnnotationsThread getInstance(Long model, Long revision = null) {
        FetchAnnotationsThread thread = new FetchAnnotationsThread()
        thread.authentication = SecurityContextHolder.context.authentication
        thread.model = model
        thread.revisionId = revision
        return thread
    }
}
