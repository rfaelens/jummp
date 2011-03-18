package net.biomodels.jummp.webapplication

import net.biomodels.jummp.webapp.menu.MenuItem

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
     * Renders a compact title of a publication.
     * Format: Journal Year Month; Volume (Issue) Pages
     * All fields are optional.
     * @param publication The PublicationTransportCommand
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
     * @attr menu The menu to render (optional)
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
}
