package net.biomodels.jummp.core

import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.TransactionStatus
import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.core.miriam.MiriamResource
import net.biomodels.jummp.core.miriam.MiriamDatatype
import net.biomodels.jummp.core.miriam.MiriamIdentifier
import net.biomodels.jummp.core.miriam.NameResolver
import org.codehaus.groovy.grails.commons.ApplicationHolder
import net.biomodels.jummp.model.Model
import org.perf4j.aop.Profiled
import net.biomodels.jummp.model.Revision
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import net.biomodels.jummp.core.miriam.GeneOntology

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService implements IMiriamService {
    /**
     * Dependency Injection of Model Service
     */
    def modelService
    /**
     * Dependency injection for ExecutorService to run threads
     */
    def executorService

    static transactional = false

    /**
     * Map of the current to be resolved Miriam URNs
     */
    private final Map<String, List<Revision>> identifiersToBeResolved = [:]
    /**
     * Lock to protect the access to the identifiersToBeResolved
     */
    private final Lock lock = new ReentrantLock()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateMiriamResources(String url, boolean force) {
        runAsync {
            MiriamResource.withTransaction { TransactionStatus status ->
                if (force) {
                    // delete all MIRIAM data
                    MiriamDatatype.list().each {
                        it.delete()
                    }
                }
                try {
                    parseResourceXML(url)
                    status.flush()
                    log.info("MIRIAM resources updated successfully from ${url}")
                } catch (Exception e) {
                    status.setRollbackOnly()
                    log.info(e.getMessage(), e)
                }
            }
        }
    }

    @Profiled(tag="MiriamService.miriamData")
    public Map miriamData(String urn) {
        int colonIndex = urn.lastIndexOf(':')
        String datatypeUrn = urn.substring(0, colonIndex)
        String identifier = urn.substring(colonIndex + 1)
        identifier = URLDecoder.decode(identifier)
        MiriamDatatype datatype = resolveDatatype(datatypeUrn)
        if (!datatype) {
            return [:]
        }
        MiriamResource preferred = preferredResource(datatype)
        if (!preferred) {
            return [:]
        }
        MiriamIdentifier resolvedName = MiriamIdentifier.findByDatatypeAndIdentifier(datatype, identifier)
        return [
                dataTypeLocation: preferred.location,
                dataTypeName: datatype.name,
                name: resolvedName ? resolvedName.name : URLCodec.decode(identifier),
                url: preferred.action.replace('$id', identifier)
        ]
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public updateAllMiriamIdentifiers() {
        runAsync {
            MiriamIdentifier.list().each { identifier ->
                Map<String, NameResolver> nameResolvers = ApplicationHolder.application.mainContext.getBeansOfType(NameResolver)
                for (NameResolver nameResolver in nameResolvers.values()) {
                    if (nameResolver.supports(identifier.datatype)) {
                        String resolvedName = nameResolver.resolve(identifier.datatype, identifier.identifier)
                        if (resolvedName && identifier.name != resolvedName) {
                            String oldName = identifier.name
                            identifier.name = resolvedName
                            identifier.save(flush: true)
                            log.info("Miriam Identifier with id '${identifier.identifier}' for datatype '${identifier.datatype.identifier}' updated from '${oldName}' to '${resolvedName}'")
                            break
                        }
                    }
                }
            }
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Profiled(tag="MiriamService.updateModels")
    public void updateModels() {
        Model.list().each {
            executorService.submit(ApplicationHolder.application.mainContext.getBean("fetchAnnotations", it.id))
        }
    }

    /**
     * Queues a Miriam URN for name resolving.
     *
     * Each URN which is currently being resolved is stored in a Map. Access to the Map
     * is protected by a lock. If the URN is currently being resolved or if it is already
     * in the database, nothing is done. In case of currently being resolved, the referenced
     * @p revision is added to the list of Revisions for this URN.
     *
     * The actual resolving is performed by starting off a new thread. This thread will call
     * @link dequeueUrnForIdentifierResolving when the name has been resolved or failed to be resolved.
     * @param urn The Miriam URN for which the name should be resolved
     * @param rev The revision where the URN was used
     * @see dequeueUrnForIdentifierResolving
     */
    @Profiled(tag="MiriamService.queueUrnForIdentifierResolving")
    void queueUrnForIdentifierResolving(String urn, Revision rev) {
        int colonIndex = urn.lastIndexOf(':')
        String datatypeUrn = urn.substring(0, colonIndex)
        String identifier = urn.substring(colonIndex + 1)
        identifier = URLDecoder.decode(identifier)
        MiriamDatatype datatype = resolveDatatype(datatypeUrn)
        if (!datatype) {
            return
        }

        Runnable runnable = null
        lock.lock()
        try {
            if (identifiersToBeResolved.containsKey(urn)) {
                if (rev) {
                    identifiersToBeResolved[urn] << rev
                }
                return
            }
            boolean found = false
            MiriamIdentifier.withNewSession {
                MiriamIdentifier existingMiriamIdentifier = MiriamIdentifier.findByDatatypeAndIdentifier(datatype, identifier)
                if (existingMiriamIdentifier) {
                    found = true
                    GeneOntology geneOntology = GeneOntology.findByDescription(existingMiriamIdentifier)
                    if (geneOntology) {
                        if (rev) {
                            rev.refresh()
                            geneOntology.addToRevisions(rev)
                        }
                        geneOntology.save(flush: true)
                    }
                }
            }
            if (found) {
                return
            }
            // create Thread
            runnable = ApplicationHolder.application.mainContext.getBean("resolveMiriamIdentifier", urn, identifier, datatype) as Runnable
            if (runnable) {
                if (rev) {
                    identifiersToBeResolved[urn] = [rev]
                } else {
                    identifiersToBeResolved[urn] = []
                }
            }
        } catch (Exception e) {
            log.debug(e.message, e)
        } finally {
            lock.unlock()
        }
        // schedule thread
        if (runnable) {
            executorService.submit(runnable)
        }
    }

    /**
     * Dequeues a resolved Miriam URN.
     *
     * This method will only be called by the thread started off by @link queueUrnForIdentifierResolving.
     * The method persists the resolved MiriamIdentifier and locks the complete operations with the lock
     * used to protect the Map of URNs to be resolved. This way it is ensured that two threads will never
     * save the same MiriamIdentifier in a concurrent situation. Also the lock ensures that a second thread
     * will not be started to resolve the identifier while the identifier is persisted in the database.
     * @param urn The URN for which the Identifier was resolved
     * @param miriam The resolved and not yet persisted miriam identifier.
     * @see queueUrnForIdentifierResolving
     */
    @Profiled(tag="MiriamService.dequeueUrnForIdentifierResolving")
    void dequeueUrnForIdentifierResolving(String urn, MiriamIdentifier miriam) {
        lock.lock()
        try {
            List<Revision> revisions = identifiersToBeResolved[urn]
            identifiersToBeResolved.remove(urn)
            if (miriam) {
                MiriamIdentifier.withNewSession {
                    miriam.save(flush: true)
                    if (miriam.datatype.identifier == "MIR:00000022") {
                        GeneOntology geneOntology = new GeneOntology(description: miriam)
                        revisions.each {
                            if (it) {
                                it.refresh()
                                geneOntology.addToRevisions(it)
                            }
                        }
                        geneOntology.save(flush: true)
                    }
                }
            }
        } catch (Exception e) {
            log.debug(e.message, e)
        } finally {
            lock.unlock()
        }
    }

    /**
     *
     * @param uri The MIRIAM uri
     * @return The MiriamDatatype for the given MIRIAM uri
     */
    MiriamDatatype resolveDatatype(String urn) {
        return MiriamDatatype.findByUrn(urn)
    }

    /**
     *
     * @param datatype The MiriamDatatype to check
     * @return The preferred Resource for the given datatype
     */
    MiriamResource preferredResource(MiriamDatatype datatype) {
        if (datatype.preferred) {
            return datatype.preferred
        }
        return (MiriamResource)(datatype.resources.findAll { !it.obsolete }.sort { it.id }?.first())
    }

    /**
     * Downloads the MIRIAM Resource description and parses it into MiriamDatatype domain objects.
     * @param url The URL to the XML file describing the MIRIAM resources
     */
    private void parseResourceXML(String url) {
        String xml = new URL(url).getText()
        def rootNode = new XmlSlurper().parseText(xml)
        rootNode.datatype.each { datatype ->
            MiriamDatatype miriam = MiriamDatatype.findByIdentifier(datatype.@id.text())
            if (!miriam) {
                miriam = new MiriamDatatype()
            }
            miriam.identifier = datatype.@id.text()
            miriam.name = datatype.name.text()
            miriam.pattern = datatype.@pattern.text()
            miriam.urn = datatype.uris.uri.find { it.@type.text() == "URN" }.text()
            datatype.synonyms.synonym.each { synonym ->
                if (!miriam.synonyms.contains(synonym.text())) {
                    miriam.synonyms << synonym.text()
                }
            }
            datatype.resources.resource.each { resource ->
                MiriamResource res = MiriamResource.findByIdentifier(resource.@id.text())
                if (!res) {
                    res = new MiriamResource()
                }
                res.identifier = resource.@id.text()
                res.location = resource.dataResource.text()
                res.action = resource.dataEntry.text()
                if (resource.@obsolete?.text() == "true") {
                    res.obsolete = true
                }
                boolean hasPreferred = false
                if (resource.@preferred?.text() == "true") {
                    hasPreferred = true
                }
                if (res.id == null || !miriam.resources.contains(res)) {
                    miriam.addToResources(res)
                } else {
                    res.save()
                }
                if (hasPreferred && miriam.preferred != res) {
                    miriam.preferred == res
                    miriam.save()
                }
            }
            miriam.save()
        }
    }
}
