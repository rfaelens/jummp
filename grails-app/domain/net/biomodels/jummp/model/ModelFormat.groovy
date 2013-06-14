package net.biomodels.jummp.model

import net.biomodels.jummp.core.model.ModelFormatTransportCommand

/**
 * @short Domain class representing a format of a Model file.
 *
 * The main purpose of this domain class is to make the registering of model
 * formats a runtime option. Plugins can save their model format when first loaded.
 * So it is possible to extend JUMMP to support more formats by just installing a new
 * plugin without any needs to adjust the core application.
 *
 * A ModelFormat consists of a unique identifier and a human readable name which can be
 * used in the UIs.
 * @author  Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 */
class ModelFormat implements Serializable {
    /**
     * A machine readable format name, to be used in the application. E.g. SBML
     */
    String identifier
    /**
     * A human readable more spoken name. E.g. Systems Biology Markup Language
     */
    String name

    static constraints = {
        identifier(unique: true, blank: false, nullable: false)
        name(blank: false, nullable: false)
    }

    ModelFormatTransportCommand toCommandObject() {
        return new ModelFormatTransportCommand(id: id, identifier: identifier, name: name)
    }
}
