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
 * @short ModelIdentifierDecorator implementation that appends a formatted date to a model id.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
class DateAppendingDecorator extends AbstractAppendingDecorator {
    /* the suffix used to decorate model identifiers. */
    final String FORMAT
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    /* semaphore for the log threshold */
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()

    protected DateAppendingDecorator() {
    }

    /**
     * An IllegalArgumentException will be thrown if @p order is negative or
     * @p format is either empty, null or represents an invalid pattern according
     * to SimpleDateFormat's contract.
     *
     * Furthermore, spaces, slashes, backslashes, hash symbols(#), commas(,), dots(.),
     * colons(:), semicolons(;), punctuation marks, quotation marks, brackets etc, although
     * allowed by SimpleDateFormat, are rejected by this method due to the impact on the URIs
     * which will use the generated model identifiers containing these characters.
     */
    public DateAppendingDecorator(short order, String format) throws IllegalArgumentException {
        boolean orderOk = validateOrderValue(order)
        if (!orderOk) {
            log.error "Invalid order $order for $this."
            throw new Exception("Incorrect position at which to insert $this")
        } else {
            ORDER = order
        }
        if (!format) {
            log.error("Cowardly refusing to create a date decorator for format $format")
            throw new IllegalArgumentException("Please use non-empty date suffixes for model ids.")
        }
        //TODO sanitise!!
        String sampleDate = new Date().format(format)
        if (IS_INFO_ENABLED) {
            log.info "Creating $this that formats ${new Date()} as $sampleDate"
        }
        nextValue = sampleDate
        FORMAT = format
    }

    /**
     * Modify model identifier @p modelIdentifier.
     */
    ModelIdentifier decorate(ModelIdentifier modelIdentifier) {
        updateNextValueIfNeeded()
        if (modelIdentifier) {
            String currentId = modelIdentifier.getCurrentId()
            if (IS_INFO_ENABLED) {
                log.info "Decorating $currentId with $nextValue."
            }
            modelIdentifier.append(nextValue)
            return modelIdentifier
        } else {
            log.warn "Undefined model identifier encountered - decorating a new one instead."
            ModelIdentifier result = new ModelIdentifier()
            result.id.append(nextValue)
            return result
        }
    }

    /**
     * This method returns false because this implementation appends a different suffix to the
     * supplied model identifier depending on today's date.
     */
    boolean isFixed() {
        return false
    }

    /**
     * Updates the value that will be appended to the next model identifier if necessary.
     */
    void refresh() {
        updateNextValueIfNeeded()
    }

    private void updateNextValueIfNeeded() {
        String currentDate = new Date().format(FORMAT)
        boolean needsUpdating = currentDate != nextValue
        if (needsUpdating) {
            if (IS_INFO_ENABLED) {
                log.info "Updating nextValue from $nextValue to $currentDate."
            }
            nextValue = currentDate
        }
    }
}

