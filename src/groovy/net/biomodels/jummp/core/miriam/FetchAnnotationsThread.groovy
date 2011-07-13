package net.biomodels.jummp.core.miriam

import net.biomodels.jummp.model.Revision
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import net.biomodels.jummp.model.Model
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.dao.DataIntegrityViolationException

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
 * ApplicationHolder.application.mainContext.getBean("fetchAnnotations", revision)
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
        revision = modelService.getLatestRevision(threadModel)
        if (revision) {
            modelFileFormatService.getAllAnnotationURNs(revision).each {
                miriamService.queueUrnForIdentifierResolving(it, revision)
            }
        }
        // clear the Authentication from the Thread's SecurityContext
        SecurityContextHolder.clearContext()
    }

    private void fetchMiriamData(List<String> urns) {
        for (urn in urns) {
            int colonIndex = urn.lastIndexOf(':')
            String datatypeUrn = urn.substring(0, colonIndex)
            String identifier = urn.substring(colonIndex + 1)
            identifier = URLDecoder.decode(identifier)
            MiriamDatatype datatype = miriamService.resolveDatatype(datatypeUrn)
            if (!datatype) {
                continue
            }
            MiriamResource preferred = miriamService.preferredResource(datatype)
            if (!preferred) {
                continue
            }
            resolveName(datatype, identifier)
        }
    }

        /**
     * Tries to resolve a name for the given MIRIAM datatype and stores in the database.
     * Uses some well known web services to connect to.
     * @param miriam The datatype
     * @param id The identifier part of the URN.
     */
    private void resolveName(MiriamDatatype miriam, String id) {
        if (MiriamIdentifier.findByDatatypeAndIdentifier(miriam, id)) {
            if (miriam.identifier == "MIR:00000022") {
                GeneOntology ontology = GeneOntology.findByDescription(MiriamIdentifier.findByDatatypeAndIdentifier(miriam, id), [lock:true])
                if (!ontology) {
                    GeneOntology geneOntology = new GeneOntology(description: MiriamIdentifier.findByDatatypeAndIdentifier(miriam, id))
                    geneOntology.addToRevisions(revision)
                    geneOntology = geneOntology.save(flush: true)
                    resolveGeneOntologyRelationships(geneOntology, null)
                    return
                }
                ontology.addToRevisions(revision)
                ontology.refresh()
                ontology.save(flush: true)
            }
            return
        }
        Map<String, NameResolver> nameResolvers = ApplicationHolder.application.mainContext.getBeansOfType(NameResolver)
        for (NameResolver nameResolver in nameResolvers.values()) {
            if (nameResolver.supports(miriam)) {
                String resolvedName = nameResolver.resolve(miriam, id)
                if (resolvedName) {
                    if (MiriamIdentifier.findByDatatypeAndIdentifier(miriam, id)) {
                        // test again
                        return
                    }
                    try {
                        MiriamIdentifier identifier = new MiriamIdentifier(identifier: id, datatype: miriam, name: resolvedName)
                        identifier.save(flush: true)

                        // is this a GeneOntology?
                        if (miriam.identifier == "MIR:00000022") {
                            GeneOntology geneOntology = new GeneOntology(description: identifier)
                            geneOntology.addToRevisions(revision)
                            geneOntology.save(flush: true)
                            resolveGeneOntologyRelationships(geneOntology, nameResolver as GeneOntologyResolver)
                        }
                    } catch (DataIntegrityViolationException e) {
                        // such an exception can be thrown in a highly concurrent situation when another running thread
                        // just resolved the name. We can savely ignore the exception
                    }
                    return
                }
            }
        }
    }

    private void resolveGeneOntologyRelationships(GeneOntology geneOntology, GeneOntologyResolver geneOntologyResolver) {
        if (!geneOntologyResolver) {
            Map<String, NameResolver> nameResolvers = ApplicationHolder.application.mainContext.getBeansOfType(NameResolver)
            for (NameResolver nameResolver in nameResolvers.values()) {
                if (nameResolver.supports(geneOntology.description.datatype)) {
                    geneOntologyResolver = nameResolver as GeneOntologyResolver
                    break
                }
            }
            if (!geneOntologyResolver) {
                return
            }
        }
        geneOntologyResolver.resolveRelationship(geneOntology)
    }

    static public FetchAnnotationsThread getInstance(Long model) {
        FetchAnnotationsThread thread = new FetchAnnotationsThread()
        thread.authentication = SecurityContextHolder.context.authentication
        thread.model = model
        return thread
    }
}
