package net.biomodels.jummp.webapplication

import net.biomodels.jummp.webapp.menu.MenuItem
import net.biomodels.jummp.webapp.miriam.MiriamDatatype

/**
 * Small TagLib to render custom tags.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class JummpTagLib {
    static namespace = "jummp"
    /**
     * Dependency Injection of Menu Service
     */
    def menuService
    /**
     * Dependency Injection for Miriam Service
     */
    def miriamService

    /**
     * Renders a compact title of a publication.
     * Format: Journal Year Month; Volume (Issue) Pages
     * All fields are optional.
     * @attr publication REQUIRED The PublicationTransportCommand
     */
    def compactPublicationTitle = { attrs ->
        def publication = attrs.publication
        String publicationSummary = publication.journal
        if (publication.year) {
            publicationSummary += " " + publication.year
            if (publication.month) {
                publicationSummary += " " + publication.month
            }
        }
        if (publication.volume) {
            publicationSummary += "; " + publication.volume
        }
        if (publication.issue) {
            publicationSummary += "(" + publication.issue + ")"
        }
        if (publication.pages) {
            publicationSummary += ": " + publication.pages
        }
        out << publicationSummary
    }

    /**
     * Renders a span element with an alert icon and initially hidden.
     */
    def errorField = { attrs ->
        out << "<span class=\"ui-icon ui-icon-alert\" style=\"display: none\"></span>"
    }

    /**
     * Renders the menu to be processed by the jQuery jdMenu plugin.
     * The menu consists of a div element with an unordered list describing the various menu entries.
     * The actual menu data is retrieved either from the MenuService or from the passed in attribute
     * menu. The menu container (div and first ul element with class jd_menu) is only rendered if the
     * menu attribute is not present. The implementation itself is recursive, so a submenu is rendered
     * by calling this tag with the sumbenu as menu attribute.
     *
     * As the menu is completely build up using ul - li - a it also works if JavaScript is disabled, though
     * it is doubtable that this is any advantage in this app. Nevertheless also the correct href are set
     * additionally to the onclick events. All onclick events have a "return false" added to prevent the
     * loading of the actual href.
     * @attr menu REQUIRED The menu to render (optional)
     */
    def menu = { attrs ->
        List<MenuItem> m = []
        boolean renderDiv = false
        if (attrs.menu) {
            m = attrs.menu
        } else {
            m = menuService.menu()
            renderDiv = true
        }
        if (renderDiv) {
            out << "<div id=\"menu\">"
            out << "<ul class=\"jd_menu\">"
        } else {
            out << "<ul>"
        }
        m.each { menuItem ->
            String link = null
            String onClick = null
            if (menuItem.controller && menuItem.action) {
                link = g.createLink(controller: menuItem.controller, action: menuItem.action)
                if (menuItem.parameters) {
                    link += "?"
                    menuItem.parameters.eachWithIndex { parameter, i ->
                        if (i > 0) {
                            link += "&"
                        }
                        link += "${parameter.key}=${parameter.value}"
                    }
                }
            }
            if (menuItem.javaScriptCallback) {
                onClick = "loadView('${link}', ${menuItem.javaScriptCallback})"
            } else if (menuItem.javaScript) {
                onClick = menuItem.javaScript
            }
            out << "<li>"
            out << "<a href=\"${link ? link : '#'}\""
            if (onClick) {
                out << " onclick=\"${onClick};return false;\""
            }
            out << ">"
            out << menuItem.text
            out << "</a>"
            if (menuItem.subMenu) {
                out << jummp.menu(menu: menuItem.subMenu)
            }
            out << "</li>"
        }
        out << "</ul>"

        if (renderDiv) {
            out << "</div>"
        }
    }

    /**
     * Renders a list of Annotations. An annotation is a map consisting of following key/value pairs:
     * @li biologicalQualifier
     * @li modelQualifier
     * @li qualifier
     * @li resources
     * A description of the keys can be found in ISbmlService.getAnnotations().
     * @attr annotations List of Annotation maps (required)
     * @attr biological Boolean attribute whether to include annotations with biological qualifier (optional, default @c true)
     * @attr model Boolean attribute whether to include annotations with model qualifier (optional, decault @c true)
     * @todo move into an SBML taglib
     */
    def annotations = { attrs ->
        boolean biologicalAnnotations = true
        boolean modelAnnotations = true
        if (attrs.containsKey("biological")) {
            biologicalAnnotations = Boolean.parseBoolean(attrs.biological)
        }
        if (attrs.containsKey("model")) {
            modelAnnotations = Boolean.parseBoolean(attrs.model)
        }
        List<String> renderedAnnotations = []
        attrs.annotations.each { annotation ->
            if (annotation.qualifier == "BQB_UNKNOWN" || annotation.qualifier == "BQM_UNKNOWN") {
                return
            }
            if (annotation.biologicalQualifier && !biologicalAnnotations) {
                return
            }
            if (annotation.modelQualifier && !modelAnnotations) {
                return
            }
            renderedAnnotations << renderAnnotation(annotation)
        }
        if (renderedAnnotations.size() == 1) {
            out << renderedAnnotations.first()
        } else {
            out << "<ul>"
            renderedAnnotations.each {
                out << "<li>${it}</li>"
            }
            out << "</ul>"
        }
    }

    /**
     * Renders one Annotations Map.
     * @see annotations
     * @todo move into an SBML taglib
     */
    def renderAnnotation = { annotation ->
        renderQualifer(annotation.qualifier)
        if (annotation.resources.size() == 1) {
            out << "&nbsp;"
            renderURN(annotation.resources[0])
        } else {
            out << "<ul>"
            annotation.resources.each {
                out << "<li>"
                renderURN(it)
                out << "</li>"
            }
            out << "</ul>"
        }
    }

    /**
     * Renders a single MIRIAM URN from an annotation resource.
     * @todo move into an SBML taglib
     */
    def renderURN = { attrs ->
        String resource
        if (attrs instanceof String) {
            resource = attrs
        } else {
            resource = attrs.resource
        }
        int colonIndex = resource.lastIndexOf(':')
        String urn = resource.substring(0, colonIndex)
        String identifier = resource.substring(colonIndex + 1)
        MiriamDatatype miriam = miriamService.resolveDatatype(urn)
        out << "<a target=\"_blank\" href=\"${miriamService.preferredResource(miriam).location}\">${miriam.name}</a>"
        out << "&nbsp;"
        out << "<a target=\"_blank\" href=\"${miriamService.preferredResource(miriam).action.replace('$id', identifier)}\">${miriamService.resolveName(miriam, identifier)}</a>"
    }

    /**
     * Renders a Biomodels.net annotation qualifier.
     * The qualifier has to be one of org.sbml.jsbml.CVTerm.Qualifier
     * @todo move into an SBML taglib
     */
    def renderQualifer = { qualifier ->
        switch (qualifier) {
        case "BQB_ENCODES":
            out << g.message(code: "sbml.qualifier.bqb.encodes")
            break
        case "BQB_HAS_PART":
            out << g.message(code: "sbml.qualifier.bqb.hasPart")
            break
        case "BQB_HAS_PROPERTY":
            out << g.message(code: "sbml.qualifier.bqb.hasProperty")
            break
        case "BQB_HAS_VERSION":
            out << g.message(code: "sbml.qualifier.bqb.hasVersion")
            break
        case "BQB_IS":
            out << g.message(code: "sbml.qualifier.bqb.is")
            break
        case "BQB_IS_DESCRIBED_BY":
            out << g.message(code: "sbml.qualifier.bqb.isDescribedBy")
            break
        case "BQB_IS_ENCODED_BY":
            out << g.message(code: "sbml.qualifier.bqb.isEncodedBy")
            break
        case "BQB_IS_HOMOLOG_TO":
            out << g.message(code: "sbml.qualifier.bqb.isHomologTo")
            break
        case "BQB_IS_PART_OF":
            out << g.message(code: "sbml.qualifier.bqb.isPartOf")
            break
        case "BQB_IS_PROPERTY_OF":
            out << g.message(code: "sbml.qualifier.bqb.isPropertyOf")
            break
        case "BQB_IS_VERSION_OF":
            out << g.message(code: "sbml.qualifier.bqb.isVersionOf")
            break
        case "BQB_OCCURS_IN":
            out << g.message(code: "sbml.qualifier.bqb.occursIn")
            break
        case "BQM_IS":
            out << g.message(code: "sbml.qualifier.bqm.is")
            break
        case "BQM_IS_DERIVED_FROM":
            out << g.message(code: "sbml.qualifier.bqm.isDerivedFrom")
            break
        case "BQM_IS_DESCRIBED_BY":
            out << g.message(code: "sbml.qualifier.bqm.isDescribedBy")
            break
        default:
            out << ""
            break
        }
    }
}
