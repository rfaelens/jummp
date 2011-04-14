package net.biomodels.jummp.webapp.menu

/**
 * @short DataType describing one menu item.
 * @author Gerold Steierl <g.steierl@dkfz.de>
 */
class MenuItem {
    /**
     * The name of the controller the menu item is pointing to.
     */
    String controller
    /**
     * The name of the controller's action the menu item is pointing to.
     * Only required if controller is set.
     */
    String action
    /**
     * Additional parameters to be added to the link.
     * Only required if controller and action are set
     */
    List<AbstractMap.SimpleEntry<String,String>> parameters
    /**
     * The name of the javascript callback to execute after loading the view
     * described by controller and action.
     * Only required if controller and action are set.
     */
    String javaScriptCallback
    /**
     * JavaScript code to execute instead of a link to controller and action.
     * May only be set if controller and action are not set.
     */
    String javaScript
    /**
     * The title of the menu entry
     */
    String text
    /**
     * A sub menu. Only required if neither controller and action nor javaScript is set
     */
    List<MenuItem> subMenu
}
