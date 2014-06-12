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

package net.biomodels.jummp.core.model.identifier.decorator

import net.biomodels.jummp.core.model.identifier.ModelIdentifier

/**
 * @short Abstract ModelIdentifierDecorator implementation defining the natural order.
 *
 * This class also prevents external access to the 'nextValue' attributes of concrete subclasses.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
abstract class AbstractAppendingDecorator implements OrderedModelIdentifierDecorator {

    abstract ModelIdentifier decorate(ModelIdentifier modelIdentifier)

    abstract boolean isFixed()

    abstract void refresh()

    /**
     * Defines the natural order for instances of OrderedModelIdentifierDecorator implementations.
     */
    @Override
    int compareTo(OrderedModelIdentifierDecorator other) {
        this.ORDER <=> other.ORDER
    }

    @Override
    String toString() {
        "${this.getClass().name} order: $ORDER nextValue: $nextValue"
    }

    /**
     * Do not allow access to nextValue outside of an implementation of
     * ModelIdentifierDecorator.
     */
    String getNextValue() {
        return null
    }

    /**
     * Do not allow nextValue to be modified outside of ModelIdentifierDecorator implementations.
     */
    void setNextValue(String ignoredValue) {
    }

    /**
     * Helper method that checks whether the supplied @p order falls within the range [0, 2^15-1).
     */
    protected boolean validateOrderValue(short order) {
        return order >= 0 && order < Short.MAX_VALUE
    }
}

