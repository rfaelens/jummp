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

    void run() {
        // set the Authentication in the Thread's SecurityContext
        SecurityContextHolder.context.setAuthentication(authentication)
        // perform the operation
        Model threadModel = Model.get(model)
        Revision revision = modelService.getLatestRevision(threadModel)
        if (revision) {
            fetchMiriamData(modelFileFormatService.getAllAnnotationURNs(revision))
        }
        // clear the Authentication from the Thread's SecurityContext
        SecurityContextHolder.clearContext()
    }

    private void fetchMiriamData(List<String> urns) {
        for (urn in urns) {
            int colonIndex = urn.lastIndexOf(':')
            String datatypeUrn = urn.substring(0, colonIndex)
            String identifier = urn.substring(colonIndex + 1)
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
                    } catch (DataIntegrityViolationException e) {
                        // such an exception can be thrown in a highly concurrent situation when another running thread
                        // just resolved the name. We can savely ignore the exception
                    }
                    return
                }
            }
        }
    }

    static public FetchAnnotationsThread getInstance(Long model) {
        FetchAnnotationsThread thread = new FetchAnnotationsThread()
        thread.authentication = SecurityContextHolder.context.authentication
        thread.model = model
        return thread
    }
}
