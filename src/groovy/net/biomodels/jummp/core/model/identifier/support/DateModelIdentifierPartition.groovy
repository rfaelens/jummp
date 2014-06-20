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

/**
 * @short Responsible for holding information about the date part of a model identifier.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class DateModelIdentifierPartition extends ModelIdentifierPartition {
    /* The pattern that the date must follow. */
    String format

    void setFormat(String fmt) {
        String today = new Date().format(fmt)
        if (today) {
            format = fmt
            width = today.length()
            value = today
        } else {
            final String M = "Date format $fmt is not appropriate. Try 'yyyyMMdd' or 'yyMMdd'."
            log.error(M)
            throw IllegalArgumentException(M)
        }
    }

    @Override
    boolean validate() {
        return validateFormat() && super.validate()
    }

    protected boolean validateFormat() {
        //TODO further restrict allowed values
        return format
    }
}
