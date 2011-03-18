package net.biomodels.jummp.webapp.menu

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.web.context.ServletContextHolder

/**
 * @short Service managing menus.
 *
 * This service parses the menu from an XML and builds the menu structure for it.
 * The menu is per set of roles and cached. This ensures that per set of roles the
 * XML is only parsed once. So for two users with the same roles the menu xml is only
 * processed once. For two users with different set of roles the menu is processed twice.
 *
 * The structure of the xml is described in the xsd.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 * @author Gerold Steierl <g.steierl@dkfz.de>
 */
class MenuService {
    boolean transactional = true
    /**
     * Dependency injection of Spring Security Service
     */
    def springSecurityService
    /**
     * Cache for the parsed menus.
     * As key a hash code of the user's roles is used. So two users with the same set of roles
     * evaluate to the same key and receive the same menu.
     */
    private Map<Integer, List<MenuItem>> menus = [:]

    /**
     * Returns the menu for the current user. In case the cache does not yet contain a menu for
     * the user's roles, a new menu is parsed from the XML description file.
     * @return Menu for current user.
     */
    public List<MenuItem> menu() {
        Integer userRoles = userRolesHash()
        if (!menus.containsKey(userRoles)) {
            menus.put(userRoles, createMenu())
        }
        return menus.get(userRoles)
    }

    /**
     * Generates the menu for the current user from XML
     * @return New menu for the user
     */
    private List<MenuItem> createMenu() {
        return parse(new XmlSlurper().parse(new File(ServletContextHolder?.servletContext?.getRealPath("menu.xml"))))
    }

    /**
     * Parses a (sub)menu from XML
     * @param records The GPathResult of the current (sub)menu structure
     * @return A parsed (sub)menu
     */
    private List<MenuItem> parse(def records) {
        List<MenuItem> menu = []
        records.menuItem?.each() { node ->
            if (isMenuItemVisible(node)) {
                MenuItem menuItem = new MenuItem()
                menuItem.setText(node.name.text())
                if (node.controller?.text() && node.action?.text()) {
                    menuItem.setController(node.controller.text())
                    menuItem.setAction(node.action?.text())
                    menuItem.setJavaScriptCallback(node.viewCallback?.text())
                    List<AbstractMap.SimpleEntry<String, String>> parameters = []
                    node.params?.param?.each() { param ->
                        parameters << new AbstractMap.SimpleEntry<String, String>(param.@name, param.@value)
                    }
                    menuItem.setParameters(parameters)
                } else if (node.subMenu) {
                    List<MenuItem> subMenu = parse(node.subMenu)
                    if (subMenu.size() > 0) {
                        menuItem.setSubMenu(subMenu)
                    }
                }
                menuItem.setJavaScript(node.javaScript?.text())
                menu << menuItem;
            }
        }
        return menu
    }

    /**
     *
     * @return hash value for the current user's roles
     */
    private Integer userRolesHash() {
        if (!springSecurityService.authentication) {
            return "ROLE_ANONYMOUS".hashCode()
        }
        List<String> roles = SpringSecurityUtils.authoritiesToRoles(springSecurityService.authentication.authorities).toList()
        Integer rolesNumber = 0
        roles.each { role ->
            rolesNumber += role.hashCode()
        }
        return rolesNumber
    }

    /**
     * Tests whether the XML element describing a menu item is visible to the current user.
     * @param menuItem The menu item to test
     * @return @c true if the user is allowed to see this menu item, @c false otherwise
     */
    private Boolean isMenuItemVisible(def menuItem) {
       List<String> roles = menuItem.visibleTo?.role*.text()
       if (roles) {
           return SpringSecurityUtils.ifAnyGranted(roles.join(","))
       } else {
           // If the XML-file that defines the menu does not deliver a single role-tag for
           // a particular menu item, then this menu item is visible to everyone
           return true
       }
    }
}
