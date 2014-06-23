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

package net.biomodels.jummp.core.model.identifier.generator

import net.biomodels.jummp.core.events.DateModelIdentifierDecoratorUpdatedEvent
import net.biomodels.jummp.core.events.ModelIdentifierDecoratorUpdatedEvent
import net.biomodels.jummp.core.model.identifier.decorator.ModelIdentifierDecorator
import net.biomodels.jummp.core.model.identifier.decorator.OrderedModelIdentifierDecorator
import net.biomodels.jummp.core.model.identifier.decorator.VariableDigitAppendingDecorator
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.SmartApplicationListener

/**
 * @short Abstract implementation for producing model identifiers.
 *
 * This class also implements the SmartApplicationListener in order to respond to events issued
 * by ModelIdentifierDecorator implementations.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
abstract class AbstractModelIdentifierGenerator implements ModelIdentifierGenerator,
            SmartApplicationListener {
    /** Required by SmartApplicationListener#getOrder() */
    int order = 10
    /** the class logger. */
    private static final Log log = LogFactory.getLog(this)
    private static final boolean IS_INFO_ENABLED = log.isInfoEnabled()
    /**
     * The registry of decorators which an implementation may use to generate model identifiers.
     */
    protected TreeSet<OrderedModelIdentifierDecorator> DECORATOR_REGISTRY

    abstract String generate()

    abstract void update()

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType instanceof ModelIdentifierDecoratorUpdatedEvent
    }

    @Override
    boolean supportsSourceType(Class<?> sourceType) {
        return sourceType instanceof ModelIdentifierDecorator
    }

    @Override
    void onApplicationEvent(ApplicationEvent decoratorUpdatedEvent) {
        if (IS_INFO_ENABLED) {
            log.info "Processing event ${decoratorUpdatedEvent.inspect()}"
        }
        if (decoratorUpdatedEvent instanceof DateModelIdentifierDecoratorUpdatedEvent) {
            // finds the first variable digit decorator and reset its value.
            VariableDigitAppendingDecorator d = DECORATOR_REGISTRY.find { d ->
                d instanceof VariableDigitAppendingDecorator
            }
            if (d) {
                final String NEW_VALUE = "1".padLeft(d.WIDTH, '0')
                d.nextValue = NEW_VALUE
                if (IS_INFO_ENABLED) {
                    log.info "Attribute 'nextValue' of $d has been reset to $NEW_VALUE."
                }
            }
        }
    }
}
