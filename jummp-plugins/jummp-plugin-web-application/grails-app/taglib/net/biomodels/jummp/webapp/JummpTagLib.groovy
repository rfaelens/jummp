package net.biomodels.jummp.webapp

class JummpTagLib {
    static namespace = "jummp"

    /**
     * Renders the HTML code for a JUMMP styled button.
     * That is including the active glow.
     * @attr id The HTML id (optional)
     * @attr class The HTML class (optional)
     **/
    def button = { attrs, body ->
        out << render(template: "/templates/buttonTemplate", model: [attrs: attrs, body: body()])
    }

}
