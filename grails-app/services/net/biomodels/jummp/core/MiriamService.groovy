package net.biomodels.jummp.core

import org.codehaus.groovy.grails.plugins.codecs.URLCodec
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.TransactionStatus
import net.biomodels.jummp.core.miriam.IMiriamService
import net.biomodels.jummp.core.miriam.MiriamUpdateException
import net.biomodels.jummp.core.miriam.MiriamResource
import net.biomodels.jummp.core.miriam.MiriamDatatype
import net.biomodels.jummp.core.miriam.MiriamIdentifier
import net.biomodels.jummp.core.miriam.NameResolver
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Service for handling MIRIAM resources.
 *
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MiriamService implements IMiriamService {

    static transactional = true

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

    public Map miriamData(String urn) {
        int colonIndex = urn.lastIndexOf(':')
        String datatypeUrn = urn.substring(0, colonIndex)
        String identifier = urn.substring(colonIndex + 1)
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

    public void fetchMiriamData(List<String> urns) {
        urns.each { urn ->
            int colonIndex = urn.lastIndexOf(':')
            String datatypeUrn = urn.substring(0, colonIndex)
            String identifier = urn.substring(colonIndex + 1)
            MiriamDatatype datatype = resolveDatatype(datatypeUrn)
            if (!datatype) {
                return
            }
            MiriamResource preferred = preferredResource(datatype)
            if (!preferred) {
                return
            }
            resolveName(datatype, identifier)
        }
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

    /**
     *
     * @param uri The MIRIAM uri
     * @return The MiriamDatatype for the given MIRIAM uri
     */
    private MiriamDatatype resolveDatatype(String urn) {
        return MiriamDatatype.findByUrn(urn)
    }

    /**
     *
     * @param datatype The MiriamDatatype to check
     * @return The preferred Resource for the given datatype
     */
    private MiriamResource preferredResource(MiriamDatatype datatype) {
        if (datatype.preferred) {
            return datatype.preferred
        }
        return (MiriamResource)(datatype.resources.findAll { !it.obsolete }.sort { it.id }?.first())
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
                    MiriamIdentifier identifier = new MiriamIdentifier(identifier: id, datatype: miriam, name: resolvedName)
                    identifier.save(flush: true)
                    return
                }
            }
        }
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
