package net.biomodels.jummp.webapp

/**
 * @short Controller to render the menu.
 * @author Martin Gräßlin <m.graesslin@dkfz.de>
 */
class MenuController {

    /**
     * Default action which renders the menu and only the menu.
     */
    def index = {
        render jummp.menu()
    }
}
