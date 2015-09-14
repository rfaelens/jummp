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

import net.biomodels.jummp.core.model.identifier.decorator.ChecksumAppendingDecorator

/**
 * @short Responsible for storing information about the checksum part of a model identifier.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class ChecksumModelIdentifierPartition extends ModelIdentifierPartition {

    ChecksumModelIdentifierPartition() {
        width = ChecksumAppendingDecorator.getChecksumWidth()
    }

    @Override
    protected boolean validateBeginIndex() {
        return beginIndex > 1
    }

    @Override
    protected boolean validateWidth() {
        return width == ChecksumAppendingDecorator.TOTAL_WIDTH
    }

    @Override
    protected boolean validateValue() {
        return value.length() == ChecksumAppendingDecorator.getChecksumWidth() &&
                    super.validateValue()
    }
}
