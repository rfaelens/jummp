package net.biomodels.jummp.core.miriam

/**
 * @short Thread to resolve the name of one Miriam Identifier.
 *
 * This thread is scheduled by the MiriamService when the name of a Miriam
 * Identifier has to be resolved. The thread is provided with the original
 * URN, the datatype the URN belongs to and the cleaned up identifier.
 *
 * The thread uses the available NameResolvers to resolve the name and passes
 * a resolved MiriamIdentifier to the MiriamService to persist.
 *
 * The MiriamService takes care of ensuring that never two threads are scheduled
 * for the same URN. It is important to not create this thread somewhere else as
 * this could result in undefined behavior.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class ResolveMiriamIdentifierThread implements Runnable {
    /**
     * Dependency Injection of Miriam Service
     */
    def miriamService
    /**
     * Dependency Injection of Grails Application
     */
    def grailsApplication

    /**
     * The Miriam Datatype this URN is for
     */
    private MiriamDatatype datatype
    /**
     * The original URN to be processed
     */
    private String urn
    /**
     * The identifier part of the URN (in human readable way)
     */
    private String identifier

    void run() {
        MiriamIdentifier miriamIdentifier = null
        try {
            Map<String, NameResolver> nameResolvers = grailsApplication.mainContext.getBeansOfType(NameResolver)
            for (NameResolver nameResolver in nameResolvers.values()) {
                if (nameResolver.supports(datatype)) {
                    String resolvedName = nameResolver.resolve(datatype, identifier)
                    if (resolvedName) {
                        miriamIdentifier = new MiriamIdentifier(identifier: identifier, datatype: datatype, name: resolvedName)
                        break
                    }
                }
            }
        } finally {
            miriamService.dequeueUrnForIdentifierResolving(urn, miriamIdentifier)
        }
    }

    static public ResolveMiriamIdentifierThread getInstance(String urn, String identifier, MiriamDatatype datatype) {
        ResolveMiriamIdentifierThread thread = new ResolveMiriamIdentifierThread()
        thread.urn = urn
        thread.identifier = identifier
        thread.datatype = datatype
        return thread
    }
}
