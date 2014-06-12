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
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * @short ModelIdentifierDecorator implementation that adds a literal suffix to all model ids.
 *
 * If model identifiers follow a predictable pattern, an * attacker might attempt to access
 * models to which they do not have access. This implementation of ModelIdentifierDecorator
 * computes a random hash and appends it to model identifiers in order to mitigate against
 * this behaviour.
 *
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
final class ChecksumAppendingDecorator extends FixedLiteralAppendingDecorator {
    /* the separator used to separate the hash from the rest of the model identifier. */
    final char SEPARATOR
    /* the default separator that precedes the auto-generated checksum. */
    final char DEFAULT_SEPARATOR = '-'
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    /* semaphore for the log threshold */
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    private ChecksumAppendingDecorator() {
    }

    /*
     * Although this decorator appends a different string to each model id, it does not
     * pre-compute the next value to be appended, hence from the perspective of external
     * components, it behaves like a fixed decorator.
     */
    public ChecksumAppendingDecorator(char sep, short order) {
        boolean orderOk = validateOrderValue(order)
        if (!orderOk) {
            log.warn "Invalid order $order for $this."
            ORDER = Short.MAX_VALUE
        } else {
            ORDER = order
        }
        if (!sep) {
            log.warn "ChecksumAppendingDecorator(char) expects a non-empty argument."
            sep = DEFAULT_SEPARATOR
        }
        SEPARATOR = sep
        if (IS_INFO_ENABLED) {
            log.info "Created $this"
        }
    }

    /**
     * Modify model identifier @p modelIdentifier.
     *
     * This decorator is expected to be the last one called for @p modelIdentifier, hence
     * the argument must not be undefined.
     */
    @Override
    ModelIdentifier decorate(ModelIdentifier modelIdentifier) {
        if (!modelIdentifier) {
            //the checksum for "" is always the same
            log.error "Undefined model identifier encountered - cannot compute checksum."
            throw new Exception("Cannot compute checksum for undefined model identifier.")
        }
        String currentId = modelIdentifier.id.toString()
        String nextValue = currentId.encodeAsSHA256().encodeAsSHA256()
        if (IS_INFO_ENABLED) {
            log.info "Decorating $currentId with $nextValue."
        }
        modelIdentifier.id.append(SEPARATOR).append(nextValue)
        return modelIdentifier
    }

    /**
     * Instances of this class are expected to be last in the decorator order and never first.
     */
    @Override
    protected boolean validateOrderValue(short value) {
        return value > 0 && value <= Short.MAX_VALUE
    }

    @Override
    String toString() {
        "${this.getClass().name}, separator: $SEPARATOR, nextValue: $nextValue, order: $ORDER"
    }
}
