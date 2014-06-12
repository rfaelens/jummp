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
 * @short Interface for influencing the generation of model identifiers.
 *
 * Implementations of this interface are expected to provide means of decorating model
 * identifiers according to user-defined configuration settings.
 *
 * Concrete implementations of this interface must also update on-demand the suffixes they use
 * to decorate a model identifier. Requests to do so will come from services which observe
 * relevant events in the application such as bootstrap, the start of the model submission
 * process, or its end.
 * @author Mihai Glonț <mihai.glont@ebi.ac.uk>
 */
interface ModelIdentifierDecorator {
    /**
     * The value that an implementation of this interface will use to decorate the next model
     * identifier.
     */
    String nextValue
    /**
     * Modify model identifier @p modelIdentifier.
     */
    ModelIdentifier decorate(ModelIdentifier modelIdentifier)
    /**
     * States whether a decorator implementation appends the same suffix to all
     * model identifiers, in which case it is considered fixed, or a different
     * suffix, in which case it is said to be variable.
     */
    boolean isFixed()
    /**
     * Updates the value of the suffix that a decorator implementation will use
     * the next time its decorate() method is called.
     */
    void refresh()
}
