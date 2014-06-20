/**
 * Copyright (C) 2010-2014 EMBL-European Bioinformatics Institute (EMBL-EBI),
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

package net.biomodels.jummp.core.model.identifier.support

import java.util.regex.Pattern

/**
 * @short Responsible for storing information about the literal part of a model identifier.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class LiteralModelIdentifierPartition extends ModelIdentifierPartition {
    /* Indicates whether a literal part of the model identifier is constant or not. */
    boolean fixed
    /* Bad patterns for the suffix */
    private static List<Pattern> EXCLUSIONS = [
        Pattern.compile("^.*\\p{Punct}.*\$"),
        Pattern.compile("^.*\\s.*\$"),
        Pattern.compile("^.*\\p{Cntrl}.*\$")
    ]

    LiteralModelIdentifierPartition(String fixedSetting, String suffix) {
        fixed = Boolean.parseBoolean(fixedSetting)
        value = suffix
        width = suffix?.length()
        if (!validateValue()) {
            throw new Exception(
                        "Literal suffix ${value.trim()} is not valid.")
        }
    }

    @Override
    boolean validate() {
        return validateFixed() && validateValue() && super.validate()
    }

    @Override
    boolean validateValue() {
        boolean valueOk = true
        EXCLUSIONS.each { p ->
            if (valueOk && value ==~ p) {
                log.error "Literal suffix ${value.trim()} is not valid because it matches $p."
                valueOk = false
            }
        }
        return valueOk
    }

    protected boolean validateFixed() {
        if (!fixed) {
            log.error "Fixed attribute for ${this.dump()} must be true."
        }
        return fixed
    }
}
