package net.biomodels.jummp.core

import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.TransactionStatus
import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.core.miriam.MiriamResource
import net.biomodels.jummp.core.miriam.MiriamDatatype
import net.biomodels.jummp.core.miriam.MiriamIdentifier
import net.biomodels.jummp.core.miriam.NameResolver
import net.biomodels.jummp.model.Model
import org.perf4j.aop.Profiled
import net.biomodels.jummp.model.ModelVersion
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.locks.Lock
import net.biomodels.jummp.core.miriam.GeneOntology
import net.biomodels.jummp.core.miriam.GeneOntologyRelationshipType
import net.biomodels.jummp.core.miriam.GeneOntologyRelationship

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService implements IMiriamService {
    /**
     * Helper class for the relation between two GeneOntologies.
     * Only used during import of Miriam data.
     */
    private class GeneOntologyRelationshipInformation {
        /**
         * The Gene Ontology identifier (GO:#####) the relation points from
         */
        String from
        /**
         * The Gene Ontology identifier (GO:#####) the relation points to.
         */
        String to
        /**
         * The relationship type between the from and to Gene Ontologies
         */
        GeneOntologyRelationshipType type

        GeneOntologyRelationshipInformation(String from, String to, GeneOntologyRelationshipType type) {
            this.from = from
            this.to = to
            this.type = type
        }
    }
    /**
     * Dependency injection for ExecutorService to run threads
     */
    def executorService
    /**
     * Dependency injection of Grails Application
     */
    @SuppressWarnings("GrailsStatelessService")
    def grailsApplication

    static transactional = false

    /**
     * Map of the current to be resolved Miriam URNs
     */
    private final Map<String, List<ModelVersion>> identifiersToBeResolved = [:]
    /**
     * List of Gene Ontology Relationships to be resolved
     */
    private final List<GeneOntologyRelationshipInformation> geneOntologyRelationships = []
    /**
     * Lock to protect the access to the identifiersToBeResolved
     */
    private final Lock lock = new ReentrantLock()

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Profiled(tag="MiriamService.updateMiriamResources")
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
                url: "http://identifiers.org/${datatype.urn.substring(datatype.urn.lastIndexOf(':') + 1)}/${identifier}"
        ]
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Profiled(tag="MiriamService.updateAllMiriamIdentifiers")
    public updateAllMiriamIdentifiers() {
        runAsync {
            MiriamIdentifier.list().each { identifier ->
                Map<String, NameResolver> nameResolvers = grailsApplication.mainContext.getBeansOfType(NameResolver)
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
            executorService.submit(grailsApplication.mainContext.getBean("fetchAnnotations", it.id))
        }
    }

    /**
     * Queues a Miriam URN for name resolving.
     *
     * Each URN which is currently being resolved is stored in a Map. Access to the Map
     * is protected by a lock. If the URN is currently being resolved or if it is already
     * in the database, nothing is done. In case of currently being resolved, the referenced
     * @p revision is added to the list of ModelVersions for this URN.
     *
     * The actual resolving is performed by starting off a new thread. This thread will call
     * @link dequeueUrnForIdentifierResolving when the name has been resolved or failed to be resolved.
     * @param urn The Miriam URN for which the name should be resolved
     * @param rev The revision where the URN was used
     * @see dequeueUrnForIdentifierResolving
     */
    @Profiled(tag="MiriamService.queueUrnForIdentifierResolving")
    void queueUrnForIdentifierResolving(String urn, ModelVersion ver) {
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
                if (ver) {
                    identifiersToBeResolved[urn] << ver
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
                        if (ver) {
                            ver.refresh()
                            geneOntology.addToVersions(ver)
                        }
                        geneOntology.save(flush: true)
                        createGeneOntologyRelationships(geneOntology, existingMiriamIdentifier.identifier)
                    }
                }
            }
            if (found) {
                return
            }
            // create Thread
            runnable = grailsApplication.mainContext.getBean("resolveMiriamIdentifier", urn, identifier, datatype) as Runnable
            if (runnable) {
                if (ver) {
                    identifiersToBeResolved[urn] = [ver]
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
            List<ModelVersion> versions = identifiersToBeResolved[urn]
            identifiersToBeResolved.remove(urn)
            if (miriam) {
                MiriamIdentifier.withNewSession {
                    miriam.save(flush: true)
                    if (miriam.datatype.identifier == "MIR:00000022") {
                        GeneOntology geneOntology = new GeneOntology(description: miriam)
                        versions.each {
                            if (it) {
                                it.refresh()
                                geneOntology.ads(it)
                            }
                        }
                        geneOntology = geneOntology.save(flush: true)
                        createGeneOntologyRelationships(geneOntology, miriam.identifier)
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
     * Queues the generation of a Gene Ontology relationship.
     * This method should be used when a Gene Ontology relationship is discovered during the creation of
     * a Miriam Identifier. It is assumed that the @p from gene ontology is already discovered and the @p to
     * gene ontology might be a new discovery.
     *
     * The maybe new gene ontology @p to is scheduled for Miriam name resolving. The actual relationship will be
     * created and persisted in the database as soon as both Gene Ontologies are resolved.
     * @param from The gene ontology identifier the relationship points from
     * @param to The gene ontology identifier the relationship points to
     * @param type The type of relationship between the two gene ontologies
     * @param toUrn The complete Miriam URN of the to gene ontology.
     */
    @Profiled(tag="MiriamService.queueGeneOntologyRelationship")
    void queueGeneOntologyRelationship(String from, String to, GeneOntologyRelationshipType type, String toUrn) {
        lock.lock()
        try {
            geneOntologyRelationships << new GeneOntologyRelationshipInformation(from, to, type)
            queueUrnForIdentifierResolving(toUrn, null)
        } catch (Exception e) {
            log.debug(e.message, e)
        } finally {
            lock.unlock()
        }
    }

    /**
     * Creates all possible gene ontology relationships between @p ontology and other already found ontologies.
     *
     * The method loops through the list of scheduled gene ontology relationships and creates a relationship if both
     * gene ontologies are already present in the database.
     * @param ontology The newly created GeneOntology
     * @param id The identifier of the GeneOntology, for convenience
     */
    private void createGeneOntologyRelationships(GeneOntology ontology, String id) {
        lock.lock()
        try {
            ontology.refresh()
            MiriamDatatype datatype = MiriamDatatype.findByIdentifier("MIR:00000022")
            ListIterator<GeneOntologyRelationshipInformation> it = geneOntologyRelationships.listIterator()
            while (it.hasNext()) {
                GeneOntologyRelationshipInformation gor = it.next()
                if (gor.from == id || gor.to == id) {
                    GeneOntology from = null
                    GeneOntology to = null
                    if (gor.from == id) {
                        from = ontology
                        MiriamIdentifier miriam = MiriamIdentifier.findByIdentifierAndDatatype(gor.to, datatype)
                        if (miriam) {
                            to = GeneOntology.findByDescription(miriam)
                        }
                    } else {
                        to = ontology
                        MiriamIdentifier miriam = MiriamIdentifier.findByIdentifierAndDatatype(gor.from, datatype)
                        if (miriam) {
                            from = GeneOntology.findByDescription(miriam)
                        }
                    }
                    if (to && from) {
                        GeneOntologyRelationship relationship = new GeneOntologyRelationship(from: from, to: to, type: gor.type)
                        if (relationship.validate()) {
                            relationship.save()
                        }
                        it.remove()
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
