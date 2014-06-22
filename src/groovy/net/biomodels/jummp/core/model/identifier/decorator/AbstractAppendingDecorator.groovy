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
import net.biomodels.jummp.core.events.ModelIdentifierDecoratorUpdatedEvent
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.spring.GrailsApplicationContext
import org.springframework.context.ApplicationEvent

/**
 * @short Abstract ModelIdentifierDecorator implementation defining the natural order.
 *
 * This class also provides a default implementation for publishing events, as required by
 * ApplicationEventPublisher.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
abstract class AbstractAppendingDecorator implements OrderedModelIdentifierDecorator {
    /**
     * The value that an implementation of this interface will use to decorate the next model
     * identifier.
     */
    String nextValue
    /* the class logger */
    private static final Log log = LogFactory.getLog(this)
    /* The main application context, set during bootstrap. */
    protected GrailsApplicationContext context
    /* the position of the decorator in the queue of a ModelIdentifierGenerator. */
    protected int ORDER

    abstract ModelIdentifier decorate(ModelIdentifier modelIdentifier)

    abstract boolean isFixed()

    abstract void refresh()

    /**
     * Publishes @p evt if it is an instance of ModelIdentifierDecoratorUpdatedEvent.
     */
    void publishEvent(ApplicationEvent evt) {
        if (!(evt instanceof ModelIdentifierDecoratorUpdatedEvent)) {
            log.warn "Banned ${this.properties} from publishing ${evt.properties}."
            return
        }
        if (log.isInfoEnabled()) {
            log.info("Publishing event ${event.properties}")
        }
        context.publishEvent(event)
    }

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
     * Helper method that checks whether the supplied @p order falls within the range [0, 2^15-1).
     */
    protected boolean validateOrderValue(int order) {
        return order >= 0 && order < Integer.MAX_VALUE
    }

    protected void setOrder(int order) {
        ORDER = order
    }
}

