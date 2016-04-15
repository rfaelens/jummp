package net.biomodels.jummp.webapp

import net.biomodels.jummp.core.annotation.ResourceReferenceTransportCommand

class DDMoReResourceReferenceTagLib {
    static namespace = "ddmore"
    static defaultEncodeAs = [taglib: 'none']

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
}
