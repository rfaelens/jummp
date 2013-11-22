/**
* Copyright (C) 2010-2013 EMBL-European Bioinformatics Institute (EMBL-EBI),
* Deutsches Krebsforschungszentrum (DKFZ)
*
* This file is part of Jummp.
*
* Jummp is free software; you can redistribute it and/or modify it under the
* terms of the GNU Affero General Public License as published by the Free
* Software Foundation; either version 3 of the License, or (at your option) any
* later version.
*
* Jummp is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
* A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
* details.
*
* You should have received a copy of the GNU Affero General Public License along
* with Jummp; if not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
**/





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
 * A ModelFormat consists of a unique identifier, a human readable name which can be
 * used in the UIs and a version that records the precise version of the format's schema.
 * @author Martin Gräßlin <m.graesslin@dkfz-heidelberg.de>
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
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
    /**
     * A unique identifier of the version of the format in which this revision is encoded.
     */
    String formatVersion

    // keep this closure transient to avoid flow scope errors
    transient beforeInsert() {
        formatVersion = formatVersion == null ? "*" : formatVersion
    }

    // DRY is nice, but duplicating one line is more efficient than another method call
    transient beforeUpdate() {
        formatVersion = formatVersion == null ? "*" : formatVersion
    }

    static constraints = {
        identifier(unique: false, blank: false, nullable: false)
        name(blank: false, nullable: false)
        formatVersion(blank: true, unique: "identifier")
    }

    ModelFormatTransportCommand toCommandObject() {
        return new ModelFormatTransportCommand(id: id, identifier: identifier, name: name, formatVersion: formatVersion)
    }
}
