package net.biomodels.jummp.webapp

import net.biomodels.jummp.core.annotation.QualifierTransportCommand
import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand

class DDMoReResourceReferenceTagLib {
    static namespace = "ddmore"

    def metadataDelegateService

    /**
     * Simple mechanism for deciding how to render resource references depending
     * on whether they point to a URI that exists or not.
     *
     * MetadataDelegateService contains a "black list" of namespaces which is used to test this.
     *
     * @attr reference REQUIRED the resource reference to render.
     */
    def filterHyperlink = { attrs, body ->
        ResourceReferenceTransportCommand xref = attrs.reference
        if (!xref) {
            return
        }
        final String uri = xref.uri
        def ignoredNamespaces = metadataDelegateService.getMetadataNamespaces()
        boolean hasBlacklistedNs = null != ignoredNamespaces.find { String ns ->
            uri?.startsWith(ns)
        }
        if (hasBlacklistedNs) {
            xref.uri = null
        }
        out << body(xref: xref)
    }

    def coalesceQualifiers = { attrs, body ->
        def annotations = attrs.annotations
        if (!annotations) {
            out << body("mergedAnnotations": [:])
        }
        TreeMap<String, List<ResourceReferenceTransportCommand>> result = new TreeMap<>()
        annotations.each { QualifierTransportCommand q,
                           List<ResourceReferenceTransportCommand> xrefs ->
            final String accession = q.accession
            if (result.containsKey(accession)) {
                result.get(accession).addAll(xrefs)
            } else {
                result.put(accession, xrefs)
            }
        }
        out << body("mergedAnnotations": result)
    }
}
